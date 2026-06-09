const express = require('express');
const router  = express.Router();

const authMiddleware   = require('../middleware/auth.middleware');
const optionalAuth     = require('../middleware/optional-auth.middleware');
const upload           = require('../middleware/upload.middleware');
const productController = require('../controllers/product.controller');

/**
 * 상품 라우트
 *
 * GET    /products          — 목록 조회 (비로그인 가능, 로그인 시 isWished 정확히 계산)
 * GET    /products/:id      — 단건 조회 (비로그인 가능)
 * POST   /products          — 등록 (로그인 + 이미지 업로드)
 * PUT    /products/:id      — 수정 (로그인 + 이미지 업로드)
 * DELETE /products/:id      — 삭제 (로그인, 판매자 본인만)
 * PATCH  /products/:id/status — 상태 변경 (로그인, 판매자 본인만)
 */

// 비로그인도 접근 가능하되 토큰이 있으면 사용자 정보 설정
router.get('/',    optionalAuth, productController.getProducts);
router.get('/:id', optionalAuth, productController.getProductById);

// 로그인 필수 + 이미지 파일 최대 5장 (multer)
router.post('/',    authMiddleware, upload.array('images', 5), productController.createProduct);
router.put('/:id',  authMiddleware, upload.array('images', 5), productController.updateProduct);

// 로그인 필수 (파일 없음)
router.delete('/:id',          authMiddleware, productController.deleteProduct);
router.patch('/:id/status',    authMiddleware, productController.updateStatus);

module.exports = router;
