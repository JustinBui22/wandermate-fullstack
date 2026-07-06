# V4 Screenshot and Demo Guide

V4 is about portfolio proof: screenshots, demo video, README media, and CV/interview material.

---

## Goal

Make the project understandable in 30–90 seconds.

A recruiter or interviewer should quickly see:

```text
- Full-stack mobile app
- Secure auth/session flow
- Trip planning domain
- Collaboration roles
- Image upload/profile polish
- Real backend tests and deployment
```

---

## Screenshot List

Recommended screenshots:

```text
1. Login screen
2. Register/OTP screen
3. My Trips with trip cover image
4. Trip Detail with cover image
5. Create/Edit Trip with cover picker
6. Destination list/detail with creator avatar
7. Activity list/detail with creator avatar
8. Collaboration tab summary
9. Pending invitations
10. Owned-trip join requests
11. Sent join requests
12. Members/role management
13. Share-code screen
14. Profile screen with uploaded avatar
15. Dark mode example
16. GitHub Actions passing
17. Render health endpoint
```

---

## Demo Video Flow

Recommended 60–90 second flow:

```text
1. Open app and login
2. Show profile picture
3. Create trip with cover image
4. Add destination
5. Add activity
6. Show creator avatar attribution
7. Open collaboration tab
8. Show invite/join request/share code
9. Switch dark mode or show theme
10. End with backend tests/CI proof
```

---

## README Media Section

Add a root README section:

```markdown
## Demo

- Backend health: <Render health URL>
- Demo video: <video link>
- Screenshots: see `/docs/media`
```

Suggested media folder:

```text
docs/media/screenshots
docs/media/demo
```

Do not commit very large video files if they make the repository heavy. Use a hosted link if needed.

---

## CV Bullet

Suggested CV bullet:

```text
Built WanderMate, a production-style full-stack travel planning app using Spring Boot, MariaDB, JWT auth, refresh/session tokens, Docker, Render, and Expo React Native. Implemented role-based trip collaboration, invitation/join request flows, share-code joining, Cloudinary image uploads, creator/editor attribution, overlap validation, and 390+ backend tests.
```
