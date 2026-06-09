const multer = require('multer');
const path   = require('path');
const fs     = require('fs');

// uploads/ 디렉토리가 없으면 자동 생성 (서버 최초 실행 시)
const uploadDir = path.join(__dirname, '../../uploads');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

/**
 * 파일 저장 설정 — multer.diskStorage
 * destination: 저장 경로 (uploads/ 폴더)
 * filename: 파일명 충돌 방지를 위해 타임스탬프 + 난수로 고유한 이름 생성
 *           원본 파일 확장자(path.extname)는 유지합니다 (예: .jpg, .png)
 */
const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, uploadDir),
  filename: (req, file, cb) => {
    // 예: 1717000000000-123456789.jpg
    const uniqueName = `${Date.now()}-${Math.round(Math.random() * 1e9)}${path.extname(file.originalname)}`;
    cb(null, uniqueName);
  },
});

/**
 * multer 인스턴스
 * limits.fileSize: 파일 1개당 최대 5MB (5 * 1024 * 1024 bytes)
 * fileFilter: 이미지 파일(image/*)만 허용 — PDF, 동영상 등은 거절
 */
const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) {
      cb(null, true); // 허용
    } else {
      cb(new Error('이미지 파일만 업로드 가능합니다.')); // 거절
    }
  },
});

module.exports = upload;
