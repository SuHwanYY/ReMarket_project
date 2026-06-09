const jwt = require('jsonwebtoken');

/**
 * 선택적 JWT 인증 미들웨어
 *
 * authMiddleware 와 달리 토큰이 없어도 통과시킵니다.
 * 토큰이 있으면 req.userId 를 설정하고, 없으면 그냥 next() 를 호출합니다.
 *
 * 사용 목적:
 * - 비로그인 사용자도 상품 목록·상세를 볼 수 있어야 하지만,
 *   로그인 사용자라면 isWished 값을 정확히 계산해줘야 합니다.
 *
 * 사용 예: router.get('/products', optionalAuth, productController.getProducts)
 */
const optionalAuth = (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (authHeader && authHeader.startsWith('Bearer ')) {
    const token = authHeader.split(' ')[1];
    try {
      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      req.userId = decoded.userId; // 로그인 사용자면 userId 설정
    } catch (e) {
      // 유효하지 않은 토큰은 조용히 무시 — 비로그인으로 처리
    }
  }

  next(); // 토큰 유무와 관계없이 항상 다음으로 진행
};

module.exports = optionalAuth;
