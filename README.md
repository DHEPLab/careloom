# CareLoom

Open-source platform for community health worker (CHW) home visiting programs. Supports stage-based curriculum delivery, visit scheduling, offline-first mobile access, and program management dashboards.

**Formerly VisitLink** -- developed in collaboration with Stanford University, University of Nevada Reno, and University of North Carolina at Chapel Hill.

## Quick Deploy

| Platform | Cost | Time | Guide |
|----------|------|------|-------|
| Docker Compose | Free (self-hosted) | 15 min | [Self-hosted guide](docs/getting-started/self-hosted-deploy.md) |
| Railway | ~$5-8/mo | 5 min | [One-click deploy](docs/getting-started/one-click-deploy.md) |
| Render | ~$14-21/mo | 10 min | [Render Blueprint](docs/getting-started/one-click-deploy.md#render) |

## What's Inside

- **Admin Dashboard** -- Web interface for program managers to design curricula, manage CHWs, and monitor visits
- **Mobile App** -- Expo/React Native app for CHWs to conduct home visits with structured curricula (Android, offline-first)
- **API Service** -- Spring Boot backend handling data, authentication, and curriculum management
- **Multi-language** -- English and Chinese (Mandarin) with extensible i18n

## Documentation

| Audience | Start Here |
|----------|------------|
| **Program implementers** (NGOs, health ministries) | [Implementer Guide](docs/implementer-guide/) |
| **Developers** contributing to CareLoom | [Developer Guide](docs/developer-guide/) |
| **Anyone** wanting to understand the architecture | [Architecture Overview](docs/getting-started/overview.md) |

## Tech Stack

- **Backend:** Java 8 / Spring Boot 2.2 / MySQL 5.7 / Flyway (62 migrations)
- **Admin Dashboard:** React 18 / TypeScript / Vite / Ant Design
- **Mobile App:** Expo / React Native / Redux
- **Infrastructure:** Docker / nginx / AWS (production)

## Local Development

```bash
git clone https://github.com/DHEPLab/careloom.git
cd careloom
docker compose up -d                    # Start MySQL on :3306
cd services/api && ./gradlew bootRun    # API on :8080
cd services/admin-web && yarn dev       # Admin dashboard on :3000
cd services/app && npx expo start       # Mobile app
```

Default credentials after first boot:

| Role | Username | Password |
|------|----------|----------|
| Super Admin | `admin` | `admin` |

See [Developer Guide](docs/developer-guide/) for detailed setup instructions.

## Roadmap

### Current (v1.x)
- [x] Stage-based curriculum delivery
- [x] Visit scheduling and tracking
- [x] Offline-first mobile app
- [x] Multi-language support (EN, ZH)
- [x] Admin dashboard for program managers
- [x] Questionnaire system with branching logic
- [x] Visit reporting and export

### Planned (v2.x)
- [ ] Spring Boot 3.x / Java 17 upgrade
- [ ] AI-enhanced content recommendations
- [ ] French language support
- [ ] Modular curriculum plugin system
- [ ] Enhanced offline sync
- [ ] iOS support

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on reporting bugs, suggesting features, and submitting code.

## License

Apache License 2.0. See [LICENSE](LICENSE).

## Citation

If you use CareLoom in research, please cite:

> [Citation TBD -- will be added when the platform paper is published]

## Related Projects

- [visit-link-infra](https://github.com/DHEPLab/visit-link-infra) -- Terraform infrastructure for production AWS deployment
