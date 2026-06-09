const { PrismaClient } = require('@prisma/client');
const bcrypt           = require('bcryptjs');
const jwt              = require('jsonwebtoken');
const { validationResult } = require('express-validator');

// PrismaClient 는 앱 전체에서 하나만 생성해 재사용합니다
// (여러 개 만들면 DB 연결 풀이 낭비됩니다)
const prisma = new PrismaClient();

/** 에러 응답을 일관된 형식으로 반환하는 헬퍼 */
const sendError = (res, statusCode, message) =>
  res.status(statusCode).json({ success: false, message });

// ── POST /auth/register ───────────────────────────────────────────────────────

/**
 * 회원가입
 * 처리 순서:
 * 1. express-validator 유효성 검사 결과 확인
 * 2. 이메일·닉네임 중복 확인
 * 3. 비밀번호 해싱 (bcrypt, saltRounds=10)
 * 4. DB 에 유저 저장
 * 5. 성공 응답 (비밀번호 제외)
 */
const register = async (req, res) => {
  // routes 에서 설정한 validation 규칙 결과 확인
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    // 여러 에러 중 첫 번째만 반환 (UX 개선 — 하나씩 안내)
    return res.status(400).json({ success: false, message: errors.array()[0].msg });
  }

  const { email, password, nickname, gender, region } = req.body;

  try {
    // 이메일 중복 확인
    if (await prisma.user.findUnique({ where: { email } })) {
      return sendError(res, 409, '이미 사용 중인 이메일입니다.');
    }

    // 닉네임 중복 확인
    if (await prisma.user.findUnique({ where: { nickname } })) {
      return sendError(res, 409, '이미 사용 중인 닉네임입니다.');
    }

    // 비밀번호 해싱 — saltRounds(10): 복잡도 조정값 (높을수록 느리지만 안전)
    // 평문 비밀번호는 절대 DB 에 저장하지 않습니다
    const hashedPassword = await bcrypt.hash(password, 10);

    const user = await prisma.user.create({
      data: { email, password: hashedPassword, nickname, gender, region },
    });

    return res.status(201).json({
      success: true,
      message: '회원가입이 완료되었습니다.',
      data: { id: user.id, email: user.email, nickname: user.nickname },
    });
  } catch (error) {
    console.error('[회원가입 오류]', error);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

// ── POST /auth/login ──────────────────────────────────────────────────────────

/**
 * 로그인
 * 처리 순서:
 * 1. 유효성 검사
 * 2. 이메일로 유저 조회
 * 3. 비밀번호 검증 (bcrypt.compare)
 * 4. JWT 토큰 발급
 * 5. 토큰 + 유저 기본 정보 반환
 */
const login = async (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ success: false, message: errors.array()[0].msg });
  }

  const { email, password } = req.body;

  try {
    const user = await prisma.user.findUnique({ where: { email } });

    // 보안상 "이메일 없음"과 "비밀번호 틀림"을 구분하지 않습니다
    // (어떤 이메일이 가입되어 있는지 추측하는 공격 방지)
    if (!user) {
      return sendError(res, 401, '이메일 또는 비밀번호가 올바르지 않습니다.');
    }

    const isPasswordValid = await bcrypt.compare(password, user.password);
    if (!isPasswordValid) {
      return sendError(res, 401, '이메일 또는 비밀번호가 올바르지 않습니다.');
    }

    // JWT 페이로드에는 최소한의 정보만 담습니다
    // (비밀번호, 개인정보 등 민감 정보는 절대 포함하지 않음)
    const token = jwt.sign(
      { userId: user.id, email: user.email },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );

    return res.status(200).json({
      success: true,
      message: '로그인 성공',
      data: {
        token,
        user: { id: user.id, email: user.email, nickname: user.nickname },
      },
    });
  } catch (error) {
    console.error('[로그인 오류]', error);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

// ── GET /auth/me ──────────────────────────────────────────────────────────────

/**
 * 내 정보 조회 (JWT 필요)
 * authMiddleware 에서 검증된 req.userId 로 유저를 조회합니다.
 * select 로 반환 필드를 명시해 비밀번호가 응답에 포함되지 않도록 합니다.
 */
const getMe = async (req, res) => {
  try {
    const user = await prisma.user.findUnique({
      where:  { id: req.userId },
      select: { id: true, email: true, nickname: true, gender: true, region: true, createdAt: true },
    });

    if (!user) return sendError(res, 404, '사용자를 찾을 수 없습니다.');

    return res.status(200).json({ success: true, data: user });
  } catch (error) {
    console.error('[내 정보 조회 오류]', error);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

module.exports = { register, login, getMe };
