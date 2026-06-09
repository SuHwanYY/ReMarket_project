const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const sendError = (res, status, message) =>
  res.status(status).json({ success: false, message });

const getBaseUrl = (req) => `${req.protocol}://${req.get('host')}`;

// ── PUT /user/profile ─────────────────────────────────────────────────────────

/**
 * 프로필 수정 — 닉네임 또는 지역을 변경합니다 (로그인 필요)
 * 두 필드 모두 선택적입니다. 변경하지 않는 필드는 body 에 포함하지 않으면 됩니다.
 */
const updateProfile = async (req, res) => {
  const { nickname, region } = req.body;

  if (!nickname && !region) {
    return sendError(res, 400, '수정할 정보를 입력해주세요.');
  }

  try {
    // 닉네임 중복 확인 — 본인의 현재 닉네임은 제외하고 다른 사용자와의 중복만 검사
    if (nickname) {
      const existing = await prisma.user.findFirst({
        where: { nickname, NOT: { id: req.userId } },
      });
      if (existing) return sendError(res, 409, '이미 사용 중인 닉네임입니다.');
    }

    const updated = await prisma.user.update({
      where: { id: req.userId },
      data: {
        ...(nickname && { nickname }),
        ...(region   && { region }),
      },
      // select: 응답에 포함할 필드만 명시 (비밀번호 제외)
      select: { id: true, email: true, nickname: true, gender: true, region: true },
    });

    return res.json({ success: true, message: '프로필이 수정되었습니다.', data: updated });
  } catch (err) {
    console.error('[프로필 수정 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

// ── GET /user/products ────────────────────────────────────────────────────────

/**
 * 내가 등록한 상품 목록 조회 (로그인 필요)
 * product.controller.js 의 formatProduct 를 재사용하지 않고 인라인으로 작성합니다.
 * (파일 간 의존성을 줄이기 위한 의도적인 선택)
 */
const getMyProducts = async (req, res) => {
  try {
    const products = await prisma.product.findMany({
      where:   { sellerId: req.userId },
      include: {
        images:    { orderBy: { order: 'asc' } },
        wishlists: { select: { userId: true } },
        seller:    { select: { id: true, nickname: true, region: true } },
      },
      orderBy: { createdAt: 'desc' },
    });

    const baseUrl = getBaseUrl(req);
    return res.json({
      success: true,
      data: products.map((p) => ({
        id:          p.id,
        title:       p.title,
        description: p.description,
        price:       p.price,
        category:    p.category,
        status:      p.status,
        images:      p.images.map((img) => `${baseUrl}/${img.imageUrl}`),
        wishCount:   p.wishlists.length,
        isWished:    false, // 내 상품이므로 찜 여부 계산 불필요
        seller:      p.seller,
        createdAt:   p.createdAt,
      })),
    });
  } catch (err) {
    console.error('[내 판매 목록 조회 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

module.exports = { updateProfile, getMyProducts };
