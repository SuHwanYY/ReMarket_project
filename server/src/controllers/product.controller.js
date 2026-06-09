const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

/** 에러 응답을 일관된 형식으로 반환하는 헬퍼 */
const sendError = (res, status, message) =>
  res.status(status).json({ success: false, message });

/**
 * 요청 프로토콜과 호스트로 베이스 URL 을 생성합니다.
 * 예: "http://10.0.2.2:3000"
 * 이미지 URL 을 완성할 때 사용합니다 (imageUrl 은 "uploads/파일명" 형태로 저장됨).
 */
const getBaseUrl = (req) => `${req.protocol}://${req.get('host')}`;

/**
 * 상품 조회 시 항상 포함할 관계 데이터
 * images: 업로드 순서(order)대로 정렬
 * wishlists: 찜한 userId 목록 (isWished 계산에 사용)
 * seller: 판매자 기본 정보 (비밀번호 제외)
 */
const productInclude = {
  images:    { orderBy: { order: 'asc' } },
  wishlists: { select: { userId: true } },
  seller:    { select: { id: true, nickname: true, region: true } },
};

/**
 * Prisma 상품 객체를 클라이언트 응답 형식으로 변환합니다.
 * @param product  Prisma 로 조회한 상품 (productInclude 포함)
 * @param userId   현재 로그인 유저 ID (isWished 계산용, 없으면 null)
 * @param baseUrl  이미지 URL 접두사 (예: "http://10.0.2.2:3000")
 */
const formatProduct = (product, userId, baseUrl) => ({
  id:          product.id,
  title:       product.title,
  description: product.description,
  price:       product.price,
  category:    product.category,
  status:      product.status,
  // DB 에 저장된 상대 경로("uploads/파일명")에 baseUrl 을 붙여 완성된 URL 반환
  images:      product.images.map((img) => `${baseUrl}/${img.imageUrl}`),
  wishCount:   product.wishlists.length,
  // 로그인 유저의 찜 여부: wishlists 배열에 현재 userId 가 있는지 확인
  isWished:    userId ? product.wishlists.some((w) => w.userId === userId) : false,
  seller:      product.seller,
  createdAt:   product.createdAt,
});

// ── GET /products ─────────────────────────────────────────────────────────────

/**
 * 상품 목록 조회 (비로그인도 가능 — optionalAuth)
 * 쿼리 파라미터로 필터링: keyword(제목 부분일치), category, minPrice, maxPrice, region
 */
const getProducts = async (req, res) => {
  const { keyword, category, minPrice, maxPrice, region } = req.query;
  const userId = req.userId || null; // optionalAuth 에서 설정된 값

  // Prisma where 조건 동적 빌드 — 파라미터가 있는 경우에만 조건 추가
  const where = {};
  if (keyword)  where.title    = { contains: keyword };
  if (category && category !== '전체') where.category = category;
  if (minPrice || maxPrice) {
    where.price = {};
    if (minPrice) where.price.gte = parseInt(minPrice);
    if (maxPrice) where.price.lte = parseInt(maxPrice);
  }
  // 지역 필터: seller(관계 테이블)의 region 필드를 조건으로 사용
  if (region && region !== '전체') where.seller = { region };

  try {
    const products = await prisma.product.findMany({
      where,
      include: productInclude,
      orderBy: { createdAt: 'desc' }, // 최신순 정렬
    });

    const baseUrl = getBaseUrl(req);
    return res.json({
      success: true,
      data: products.map((p) => formatProduct(p, userId, baseUrl)),
    });
  } catch (err) {
    console.error('[상품 목록 조회 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

// ── GET /products/:id ─────────────────────────────────────────────────────────

/** 상품 단건 조회 (비로그인도 가능) */
const getProductById = async (req, res) => {
  const productId = parseInt(req.params.id);
  const userId    = req.userId || null;

  try {
    const product = await prisma.product.findUnique({
      where:   { id: productId },
      include: productInclude,
    });

    if (!product) return sendError(res, 404, '상품을 찾을 수 없습니다.');

    return res.json({
      success: true,
      data: formatProduct(product, userId, getBaseUrl(req)),
    });
  } catch (err) {
    console.error('[상품 상세 조회 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

// ── POST /products ────────────────────────────────────────────────────────────

/**
 * 상품 등록 (로그인 필요)
 * 텍스트 필드는 req.body, 이미지 파일은 req.files (multer 처리)
 */
const createProduct = async (req, res) => {
  const { title, description, price, category } = req.body;
  const files = req.files || [];

  if (!title || !price || !category) {
    return sendError(res, 400, '제목, 가격, 카테고리는 필수입니다.');
  }

  try {
    const product = await prisma.product.create({
      data: {
        title,
        description: description || null,
        price:       parseInt(price),
        category,
        sellerId:    req.userId, // authMiddleware 에서 설정된 현재 사용자 ID
        images: {
          // 업로드된 파일들을 ProductImage 레코드로 일괄 생성
          // order 필드로 업로드 순서를 보존합니다
          create: files.map((file, index) => ({
            imageUrl: `uploads/${file.filename}`,
            order:    index,
          })),
        },
      },
      include: productInclude,
    });

    return res.status(201).json({
      success: true,
      message: '상품이 등록되었습니다.',
      data: formatProduct(product, req.userId, getBaseUrl(req)),
    });
  } catch (err) {
    console.error('[상품 등록 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

// ── PUT /products/:id ─────────────────────────────────────────────────────────

/**
 * 상품 수정 (판매자 본인만 가능)
 * 이미지 처리 정책:
 *   - 새 파일이 있으면 기존 이미지를 전부 삭제하고 새 파일로 교체
 *   - 새 파일이 없으면 기존 이미지를 그대로 유지
 */
const updateProduct = async (req, res) => {
  const productId = parseInt(req.params.id);
  const { title, description, price, category } = req.body;
  const files = req.files || [];

  try {
    const product = await prisma.product.findUnique({ where: { id: productId } });
    if (!product) return sendError(res, 404, '상품을 찾을 수 없습니다.');
    if (product.sellerId !== req.userId) return sendError(res, 403, '권한이 없습니다.');

    // 변경할 필드만 updateData 에 포함 (undefined 는 Prisma 에서 무시됨)
    const updateData = {};
    if (title)       updateData.title       = title;
    if (description !== undefined) updateData.description = description;
    if (price)       updateData.price       = parseInt(price);
    if (category)    updateData.category    = category;

    if (files.length > 0) {
      // 기존 이미지 레코드 전체 삭제 후 새 이미지로 교체
      await prisma.productImage.deleteMany({ where: { productId } });
      updateData.images = {
        create: files.map((file, index) => ({
          imageUrl: `uploads/${file.filename}`,
          order:    index,
        })),
      };
    }

    const updated = await prisma.product.update({
      where:   { id: productId },
      data:    updateData,
      include: productInclude,
    });

    return res.json({
      success: true,
      message: '상품이 수정되었습니다.',
      data: formatProduct(updated, req.userId, getBaseUrl(req)),
    });
  } catch (err) {
    console.error('[상품 수정 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

// ── DELETE /products/:id ──────────────────────────────────────────────────────

/** 상품 삭제 (판매자 본인만 가능) */
const deleteProduct = async (req, res) => {
  const productId = parseInt(req.params.id);

  try {
    const product = await prisma.product.findUnique({ where: { id: productId } });
    if (!product) return sendError(res, 404, '상품을 찾을 수 없습니다.');
    if (product.sellerId !== req.userId) return sendError(res, 403, '권한이 없습니다.');

    // Prisma 스키마에서 onDelete: Cascade 를 설정했다면 관련 이미지·찜도 자동 삭제됩니다
    await prisma.product.delete({ where: { id: productId } });

    return res.json({ success: true, message: '상품이 삭제되었습니다.' });
  } catch (err) {
    console.error('[상품 삭제 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

// ── PATCH /products/:id/status ────────────────────────────────────────────────

/**
 * 판매 상태 변경 (판매자 본인만 가능)
 * 허용되는 status 값: ON_SALE, RESERVED, SOLD_OUT
 */
const updateStatus = async (req, res) => {
  const productId = parseInt(req.params.id);
  const { status } = req.body;

  const validStatuses = ['ON_SALE', 'RESERVED', 'SOLD_OUT'];
  if (!validStatuses.includes(status)) {
    return sendError(res, 400, '유효하지 않은 상태값입니다.');
  }

  try {
    const product = await prisma.product.findUnique({ where: { id: productId } });
    if (!product) return sendError(res, 404, '상품을 찾을 수 없습니다.');
    if (product.sellerId !== req.userId) return sendError(res, 403, '권한이 없습니다.');

    const updated = await prisma.product.update({
      where:   { id: productId },
      data:    { status },
      include: productInclude,
    });

    return res.json({
      success: true,
      message: '상태가 변경되었습니다.',
      data: formatProduct(updated, req.userId, getBaseUrl(req)),
    });
  } catch (err) {
    console.error('[상태 변경 오류]', err);
    return sendError(res, 500, '서버 오류가 발생했습니다.');
  }
};

module.exports = { getProducts, getProductById, createProduct, updateProduct, deleteProduct, updateStatus };
