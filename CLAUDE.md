# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Backend (Spring Boot 2.7, JDK 8+)

```bash
cd backend
mvn spring-boot:run              # Start dev server on :8080
mvn clean compile                # Compile only
mvn clean package -DskipTests    # Build JAR
mvn test                         # Run tests
java -jar target/blog-backend-1.0.0.jar  # Run packaged JAR
```

### Frontend (Vue 3, Vite)

```bash
cd frontend
npm run dev                      # Start dev server on :5173
npm run build                    # Production build to dist/
npm run preview                  # Preview production build
```

### Database

```bash
# Reset database (requires MySQL running, root/root)
mysql -u root -proot --default-character-set=utf8mb4 -e \
  "DROP DATABASE IF EXISTS blog; CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -proot --default-character-set=utf8mb4 blog < backend/src/main/resources/db/schema.sql
```

## Architecture

### Request Flow

Frontend (`:5173`) → Vite dev proxy (`/api` → `:8080`) → Spring Boot controllers → MyBatis Plus mappers → MySQL.

Production: Build frontend to static files, serve via Nginx, proxy `/api` to Spring Boot.

### Authentication

- JWT stateless auth. Token stored in `localStorage` key `token`, sent as `Authorization: Bearer <token>`.
- Backend: `JwtAuthenticationFilter` validates token on every request. `SecurityConfig` allows open GET access; `/api/v1/admin/**` requires `ROLE_ADMIN`.
- Frontend: `router.beforeEach` checks `localStorage.getItem('token')` for `/admin` routes; axios interceptor redirects to `/admin/login` on 401.
- Default admin: `admin` / `admin123`.

### API Response Contract

All endpoints return `Result<T>` (`{code, message, data}`). `code=200` means success. The axios response interceptor unwraps `res.data` automatically, so frontend API functions return the payload directly.

### Backend Patterns

- **Layered architecture**: Controller → Service interface → ServiceImpl → Mapper (MyBatis Plus) → Entity.
- **Pagination**: Use `Page<Article>` as both parameter and return type for XML mappers; MyBatis Plus interceptor fills pagination metadata automatically.
- **Auto-fill timestamps**: `MyMetaObjectHandler` fills `createdAt`/`updatedAt` on insert/update if the entity has those setters. It uses `metaObject.hasSetter()` to skip entities that lack the field (e.g. `Comment` has no `updatedAt`).
- **Security filters**: `SqlInjectionFilter` and `XssFilter` (`@WebFilter("/api/*")`) run before Spring Security. `HtmlUtils.sanitize()` strips `<script>`, event handlers, and `javascript:` from HTML content.
- **Global exception handling**: `GlobalExceptionHandler` catches `BlogException`, auth failures, and generic exceptions, returning unified `Result` responses.

### Frontend Patterns

- **API modules**: `src/api/*.js` files export thin wrappers around axios (e.g. `getArticles(params)`). Public and admin endpoints coexist in the same module.
- **Pinia stores**: `auth.js` persists token/user to `localStorage`. `article.js` caches categories/tags/visitor stats globally.
- **Article type dual rendering**: `ArticleDetailView` switches CSS class between `article-tech` (sans-serif, code blocks) and `article-life` (serif, warm spacing) based on `article.articleType`. The actual HTML content is rendered via `v-html` with DOMPurify sanitization on the frontend.
- **Admin layout**: `AdminLayout.vue` wraps dashboard pages with a sidebar menu. `ArticleEditor.vue` uses WangEditor v5 (WYSIWYG) and stores HTML directly in `article.content`.

### Important File Paths

- Backend config: `backend/src/main/resources/application.yml` (dev/prod profiles, JWT secret, DB config)
- Backend mappers XML: `backend/src/main/resources/mapper/xml/`
- Database schema + seed data: `backend/src/main/resources/db/schema.sql`
- Frontend proxy: `frontend/vite.config.js` (`/api` → `http://localhost:8080`)
- Frontend API base: `frontend/src/utils/request.js`
- Article styles: `frontend/src/assets/styles/global.scss` (`.article-tech`, `.article-life`)

## Key Constraints

- **JDK 8, Spring Boot 2.7**: Do not use Java 9+ APIs (`List.of`, `Stream.toList`, `var`). Do not use Spring Boot 3 / Jakarta EE packages.
- **MySQL utf8mb4**: Database and connection must use `utf8mb4` or emoji in comments/articles will fail.
- **JJWT 0.11.x API**: Use `Jwts.builder().setSubject().setIssuedAt().setExpiration().signWith(key, SignatureAlgorithm.HS256)` and `Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)`.
- **WangEditor Vue 3 package**: Uses `@wangeditor/editor-for-vue@next` (v5 Vue 3 adapter), not the v2 package.
