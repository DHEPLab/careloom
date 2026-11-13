# CareLoom

Open-source platform for community health worker (CHW) home visiting programs. Supports stage-based curriculum delivery, visit scheduling, offline-first mobile access, and program management dashboards.

**Formerly VisitLink** -- developed in collaboration with Stanford University, University of Nevada Reno, and University of North Carolina at Chapel Hill.

## Start here

CareLoom source is available under Apache 2.0. A source checkout is not a prebuilt Android installer or a hosted service. Component version strings do not define a published platform release; use the exact commit when reporting a problem or reproducing a build.

| Goal | Guide |
|------|-------|
| Run a local instance with synthetic data | [Self-hosted setup](docs/getting-started/self-hosted-deploy.md) |
| Develop or test a component | [Local development](docs/developer-guide/local-setup.md) and [testing](docs/developer-guide/testing.md) |
| Evaluate a cloud deployment | [Cloud deployment notes](docs/getting-started/one-click-deploy.md), not yet validated as a one-click installation |
| Read the Healthy Future curriculum | [Healthy Future resources](https://dhep.org/healthy-future/#curriculum) |

The curriculum PDF is a separate resource with its own attribution and license. To deliver content in CareLoom, configure structured modules through the [curriculum editor](docs/implementer-guide/curriculum-management.md); downloading the PDF does not populate the app.

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

## Local development

Follow [Local Setup](docs/developer-guide/local-setup.md) rather than starting services against an existing deployment. Use an isolated database, your own API endpoint and synthetic records. The Android app is built separately from the API and dashboard.

Offline use requires an initial online sign-in and prepared content. See [mobile offline behavior](docs/developer-guide/mobile-app.md#offline-support) before relying on cached records or switching accounts.

## Roadmap

### Current capabilities
- [x] Stage-based curriculum delivery
- [x] Visit scheduling and tracking
- [x] Mobile caching and queued uploads; see the offline behavior and test guidance
- [x] Multi-language support (EN, ZH)
- [x] Admin dashboard for program managers
- [x] Questionnaire system with branching logic
- [x] Visit reporting and export

### Planned work
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

A platform-paper citation has not been established in this repository. For reproducibility, record the CareLoom repository URL and exact commit you used. Studies linked from the [Healthy Future page](https://dhep.org/healthy-future/#studies) evaluate their described interventions; they do not validate every software feature.

## Related Projects

- [visit-link-infra](https://github.com/DHEPLab/visit-link-infra) -- Terraform infrastructure for production AWS deployment
