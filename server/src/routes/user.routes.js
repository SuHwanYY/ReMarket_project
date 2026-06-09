const express = require('express');
const router  = express.Router();

const authMiddleware = require('../middleware/auth.middleware');
const userController = require('../controllers/user.controller');

/**
 * 유저 라우트 — 모든 엔드포인트에 로그인 필요
 *
 * PUT /user/profile   — 닉네임·지역 수정
 * GET /user/products  — 내가 등록한 상품 목록
 */
router.put('/profile',   authMiddleware, userController.updateProfile);
router.get('/products',  authMiddleware, userController.getMyProducts);

module.exports = router;
