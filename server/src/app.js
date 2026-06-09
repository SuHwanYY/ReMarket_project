require('dotenv').config(); // .env 파일의 환경변수를 process.env 로 로드

const path = require('path');
const express = require('express');
const cors = require('cors');

// 라우터 모듈 임포트 — 각 도메인별로 파일을 분리해 관리합니다
const authRoutes    = require('./routes/auth.routes');
const productRoutes = require('./routes/product.routes');
const wishlistRoutes = require('./routes/wishlist.routes');
const userRoutes    = require('./routes/user.routes');

const app = express();
const PORT = process.env.PORT || 3000;

// ── 전역 미들웨어 ─────────────────────────────────────────────────────────────

// CORS: 안드로이드 에뮬레이터(10.0.2.2)에서 이 서버로의 요청을 허용합니다
// 실제 배포 시에는 허용할 Origin 을 명시적으로 제한해야 합니다
app.use(cors());

// JSON 바디 파싱 — Content-Type: application/json 요청을 req.body 로 파싱
app.use(express.json());

// ── 정적 파일 서빙 ────────────────────────────────────────────────────────────

// 업로드된 이미지 파일을 /uploads 경로로 공개합니다
// 예: http://10.0.2.2:3000/uploads/1234567890-123.jpg
// __dirname 은 이 파일(app.js)이 위치한 src/ 디렉토리를 가리킵니다
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// ── API 라우트 등록 ────────────────────────────────────────────────────────────

app.use('/auth',     authRoutes);     // 회원가입, 로그인, 내 정보
app.use('/products', productRoutes);  // 상품 CRUD, 상태 변경
app.use('/wishlist', wishlistRoutes); // 찜 추가/제거/조회
app.use('/user',     userRoutes);     // 프로필 수정, 내 판매 목록

// ── 헬스 체크 ─────────────────────────────────────────────────────────────────

// 서버가 정상 동작하는지 확인하는 엔드포인트 (CI/CD 또는 앱 초기 연결 테스트용)
app.get('/health', (req, res) => {
  res.json({ success: true, message: 'ReMarket 서버가 정상 동작 중입니다.' });
});

// ── 에러 핸들러 ───────────────────────────────────────────────────────────────

// 404: 등록되지 않은 경로로 요청이 들어왔을 때
app.use((req, res) => {
  res.status(404).json({ success: false, message: '요청한 경로를 찾을 수 없습니다.' });
});

// 500: 예상치 못한 서버 에러 (next(err) 로 전달된 에러를 여기서 처리)
// 파라미터가 4개(err, req, res, next)여야 Express 가 에러 핸들러로 인식합니다
app.use((err, req, res, next) => {
  console.error('[서버 오류]', err.stack);
  res.status(500).json({ success: false, message: '서버 내부 오류가 발생했습니다.' });
});

app.listen(PORT, () => {
  console.log(`✅ 서버가 포트 ${PORT}에서 실행 중입니다.`);
});
