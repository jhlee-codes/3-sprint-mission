-- 1. 데이터베이스 생성
CREATE DATABASE discodeit;

-- 2. 사용자 생성
CREATE USER discodeit_user WITH PASSWORD 'discodeit1234';

-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS discodeit;

-- 3. 권한 부여 (모든 권한 부여 시)
GRANT ALL PRIVILEGES ON DATABASE discodeit TO discodeit_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO discodeit_user;
--GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA discodeit TO discodeit_user;
