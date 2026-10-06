# MEDISYNC - Git Workflow

This project rigorously follows a feature-branch workflow to maintain a clean, stable codebase and prevent direct development on the `main` branch.

## Branches
- `main`: The stable, production-ready state of the repository.
- `develop`: The primary integration branch. All feature branches are derived from and merged back into this branch.
- `feature/*`: Temporary branches used by developers for specific features, fixes, or documentation tasks.

## Workflow Rules
1. **Never direct development on main**: Always work in a feature branch or `develop`.
2. **Feature branches start from develop**: Before creating a new branch, always pull the latest `develop`.
3. **Use atomic commits**: Commits should represent a single logical change.
4. **Review before merging**: Use Pull Requests (PRs) and peer reviews before merging into `develop`.
5. **Delete feature branch after merge**: Keep the repository clean by deleting branches once merged, when appropriate.
6. **Keep main stable**: Only heavily tested code from `develop` is merged into `main`.

## Standard Workflow Process

```text
develop
   ↓
feature/<feature-name>
   ↓
commit
   ↓
push
   ↓
Pull Request
   ↓
review
   ↓
develop
   ↓
testing
   ↓
main
```

## Commit Message Conventions
We use standardized commit prefixes to clearly indicate the nature of the change:

- `feat:` A new feature.
- `fix:` A bug fix.
- `test:` Adding missing tests or correcting existing tests.
- `docs:` Documentation only changes.
- `refactor:` A code change that neither fixes a bug nor adds a feature.
- `style:` Changes that do not affect the meaning of the code (white-space, formatting).
- `chore:` Updates to build tasks, package manager configs, etc.

**Examples:**
- `feat: implement patient medicine CRUD`
- `fix: prevent negative inventory`
- `docs: finalize database design`
- `test: add authentication tests`
