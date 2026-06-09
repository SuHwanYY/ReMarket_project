const jwt = require('jsonwebtoken');

/**
 * JWT 인증 미들웨어 — 보호된 라우트에 적용
 *
 * 요청 헤더에서 토큰을 꺼내 검증하고,
 * 성공하면 req.userId 를 설정한 뒤 다음 핸들러로 넘깁니다.
 *
 * 사용법: router.get('/me', authMiddleware, controller)
 * 헤더 형식: Authorization: Bearer <JWT 토큰>
 */
const authMiddleware = (req, res, next) => {
  // 1. Authorization 헤더 확인
  const authHeader = req.headers.authorization;

  // 헤더가 없거나 "Bearer " 형식이 아니면 즉시 거절
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      success: false,
      message: '인증 토큰이 필요합니다. (Authorization: Bearer <token>)',
    });
  }

  // 2. "Bearer " 이후의 실제 토큰 값 추출
  const token = authHeader.split(' ')[1];

  try {
    // 3. 토큰 검증 — 시크릿 키가 틀리거나 만료된 경우 예외 발생
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    // 4. 디코딩된 userId 를 req 에 저장 → 이후 컨트롤러에서 req.userId 로 접근
    req.userId = decoded.userId;

    // 5. 다음 미들웨어 또는 컨트롤러로 이동
    next();
  } catch (error) {
    // 토큰 만료와 일반 오류를 구분해서 안내 (TokenExpiredError 는 jsonwebtoken 이 던지는 에러 타입)
    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        message: '토큰이 만료되었습니다. 다시 로그인해주세요.',
      });
    }

    // 위변조, 잘못된 형식 등 기타 토큰 오류
    return res.status(401).json({
      success: false,
      message: '유효하지 않은 토큰입니다.',
    });
  }
};

module.exports = authMiddleware;
