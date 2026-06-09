const express = require('express');
const router  = express.Router();

const authMiddleware      = require('../middleware/auth.middleware');
const wishlistController  = require('../controllers/wishlist.controller');

/**
 * 찜 라우트 — 모든 엔드포인트에 로그인 필요
 *
 * GET    /wishlist/:productId — 내 찜 목록 조회
 * POST   /wishlist/:productId — 찜 추가
 * DELETE /wishlist/:productId — 찜 제거
 */
router.get('/',               authMiddleware, wishlistController.getWishlist);
router.post('/:productId',    authMiddleware, wishlistController.addWishlist);
router.delete('/:productId',  authMiddleware, wishlistController.removeWishlist);

module.exports = router;
