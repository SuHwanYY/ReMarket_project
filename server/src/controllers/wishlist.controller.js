const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const sendError = (res, status, message) =>
  res.status(status).json({ success: false, message });

const getBaseUrl = (req) => `${req.protocol}://${req.get('host')}`;

// ── GET /wishlist ─────────────────────────────────────────────────────────────

/**
 * 내 찜 목록 조회 (로그인 필요)
 * Wishlist 테이블에서 현재 유저의 레코드를 가져오고,
 * 연결된 상품 정보를 product.images, product.wishlists, product.seller 와 함께 반환합니다.
 */
const getWishlist = async (req, res) => {
  try {
    const wishlists = await prisma.wishlist.findMany({
      where: { userId: req.userId },
      include: {
        product: {
          include: {
            images:    { orderBy: { order: 'asc' } },
            wishlists: { select: { userId: true } },
            seller:    { select: { id: true, nickname: true, region: true } },
          },
        },
      },
      orderBy: { createdAt: 'desc' }, // 최근 찜한 순서로 정렬
    });

    const baseUrl = getBaseUrl(req);
    return res.json({
      success: true,
      data: wishlists.map((w) => ({
        id:          w.product.id,
        title:       w.product.title,
        description: w.product.description,
        price:       w.product.price,
        category:    w.product.category,
        status:      w.product.status,
        images:      w.product.images.map((img) => `${baseUrl}/${img.imageUrl}`),
        wishCount:   w.product.wishlists.length,
        isWished:    true, // 찜 목록에 있으므로 항상 true
        seller:      w.product.seller,
        createdAt:   w.product.createdAt,
      })),
    });
  } catch (err) {
    console.error('[찜 목록 조회 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

// ── POST /wishlist/:productId ─────────────────────────────────────────────────

/**
 * 찜 추가 (로그인 필요)
 * Wishlist 테이블의 (userId, productId) 쌍은 유니크 제약이 있어
 * 이미 찜한 경우 Prisma 가 P2002(Unique Constraint Violation) 오류를 던집니다.
 */
const addWishlist = async (req, res) => {
  const productId = parseInt(req.params.productId);

  try {
    const product = await prisma.product.findUnique({ where: { id: productId } });
    if (!product) return sendError(res, 404, '상품을 찾을 수 없습니다.');

    await prisma.wishlist.create({
      data: { userId: req.userId, productId },
    });

    return res.status(201).json({ success: true, message: '찜 목록에 추가되었습니다.' });
  } catch (err) {
    // P2002: 이미 찜한 상품 (유니크 제약 위반)
    if (err.code === 'P2002') {
      return sendError(res, 409, '이미 찜한 상품입니다.');
    }
    console.error('[찜 추가 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

// ── DELETE /wishlist/:productId ───────────────────────────────────────────────

/**
 * 찜 제거 (로그인 필요)
 * deleteMany 를 사용해 해당 (userId, productId) 레코드가 없어도 오류 없이 처리합니다.
 */
const removeWishlist = async (req, res) => {
  const productId = parseInt(req.params.productId);

  try {
    await prisma.wishlist.deleteMany({
      where: { userId: req.userId, productId },
    });

    return res.json({ success: true, message: '찜 목록에서 제거되었습니다.' });
  } catch (err) {
    console.error('[찜 제거 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

module.exports = { getWishlist, addWishlist, removeWishlist };
