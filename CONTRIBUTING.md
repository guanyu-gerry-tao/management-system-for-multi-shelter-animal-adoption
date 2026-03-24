# Contributing Guide

## Team

| Name | GitHub | Role |
|---|---|---|
| Guanyu Tao (Gerry) | [@guanyu-gerry-tao](https://github.com/guanyu-gerry-tao) | Service Layer + AI Integration |
| Yiying Xie (Irene) | [@Bestpart-Irene](https://github.com/Bestpart-Irene) | Domain Layer |
| Yuxi Ou (Molly) | [@mollyouyuxi](https://github.com/mollyouyuxi) | Strategy Layer + MatchingService |

Each member is responsible for writing unit tests for their own modules.

## Branch Strategy

```
main    ← stable, accepts PRs only
dev     ← integration branch, merge features here first
feat/*  ← feature development (e.g. feat/animal-domain)
fix/*   ← bug fixes (e.g. fix/null-check-shelter)
```

All development should branch off `dev`, not `main`.

## Workflow

1. Create a branch from `dev`
2. Develop and test locally
3. Open a PR into `dev`
4. At least one teammate must review and approve before merging
5. `dev` is merged into `main` at major milestones

## Commit Messages

Use the following prefixes:

```
feat:   new feature (e.g. feat: add Animal base class)
fix:    bug fix (e.g. fix: handle null animal in Shelter)
test:   tests only (e.g. test: add BreedStrategy unit tests)
docs:   documentation (e.g. docs: add Javadoc to AdoptionService)
chore:  config, build, cleanup
```

For code standards, see [CLAUDE.md](CLAUDE.md).
