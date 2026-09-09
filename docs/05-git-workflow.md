# MEDISYNC - Git Workflow

This project strictly follows a feature-branch workflow to ensure code quality and stability.

## Branches
- `main`: The production-ready state of the repository. Only thoroughly tested code is merged here.
- `develop`: The primary integration branch. All feature branches branch off from here and merge back here.
- `feature/*`: Temporary branches used by developers to work on specific features or bug fixes (e.g., `feature/user-auth`, `feature/inventory-ui`).

## Workflow Steps

### 1. Feature Branches
When starting a new task, create a new feature branch derived from `develop`:
```bash
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name
```

### 2. Commits
- Make atomic, logical commits.
- Use clear, descriptive commit messages.
- Example: `feat: implement user registration endpoint` or `fix: correct typo in SQL schema`.

### 3. Pull Requests (PR)
- Once the feature is complete and locally tested, push the branch to the remote repository.
- Open a Pull Request from `feature/your-feature-name` against the `develop` branch.
- Include a description of the changes, testing steps, and any related issue numbers in the PR description.

### 4. Code Review
- Another team member must review the code.
- Feedback should be addressed by pushing new commits to the same feature branch.
- Automated tests (if CI is configured) must pass.

### 5. Merge into Develop
- Once approved, the PR is merged into `develop`.
- The feature branch can then be deleted.

### 6. Final Merge into Main
- Before a release, `develop` is heavily tested (integration, staging).
- Once stabilized, a Pull Request is opened from `develop` to `main`.
- Merging into `main` constitutes a production release and is usually tagged with a version number (e.g., `v1.0.0`).
