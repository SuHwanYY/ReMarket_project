const express = require('express');
const router  = express.Router();
const { body } = require('express-validator');

const authController = require('../controllers/auth.controller');
const authMiddleware = require('../middleware/auth.middleware');

/**
 * 요청 바디 유효성 검사 규칙 정의 (express-validator)
 * 컨트롤러에서 validationResult(req) 로 결과를 꺼내 씁니다.
 * 서버 측 검사이므로 클라이언트 측 검사를 우회해도 안전하게 처리됩니다.
 */

const registerValidation = [
  body('email')
    .isEmail().withMessage('올바른 이메일 형식이 아닙니다.')
    .normalizeEmail(), // 소문자 변환 등 정규화

  body('password')
    .isLength({ min: 8 }).withMessage('비밀번호는 최소 8자 이상이어야 합니다.')
    .matches(/[0-9]/).withMessage('비밀번호에 숫자가 포함되어야 합니다.')
    .matches(/[a-zA-Z]/).withMessage('비밀번호에 영문자가 포함되어야 합니다.'),

  body('nickname')
    .trim()
    .isLength({ min: 2, max: 20 }).withMessage('닉네임은 2자 이상 20자 이하여야 합니다.'),

  body('gender')
    .isIn(['M', 'W']).withMessage('성별은 M(남성) 또는 W(여성)만 허용됩니다.'),

  body('region')
    .trim()
    .notEmpty().withMessage('지역을 입력해주세요.'),
];

const loginValidation = [
  body('email')
    .isEmail().withMessage('올바른 이메일 형식이 아닙니다.')
    .normalizeEmail(),

  body('password')
    .notEmpty().withMessage('비밀번호를 입력해주세요.'),
];

// ── 라우트 등록 ───────────────────────────────────────────────────────────────

// POST /auth/register — 유효성 검사 → 컨트롤러 순서로 미들웨어 체인 실행
router.post('/register', registerValidation, authController.register);

// POST /auth/login — JWT 토큰 발급
router.post('/login', loginValidation, authController.login);

// GET /auth/me — JWT 인증 후 내 정보 반환
router.get('/me', authMiddleware, authController.getMe);

module.exports = router;
