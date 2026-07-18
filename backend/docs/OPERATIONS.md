# Operations

Operational notes for running, validating and sharing WanderMate.

## Local backend

```bash
cd backend
./mvnw spring-boot:run
```

## Local frontend

```bash
cd frontend
npm install
npm run start
```

If Expo cannot load assets over LAN, use:

```bash
npx expo start --tunnel -c
```

## Health check

```text
GET /The-Project/api/v1/health
```

Live Render check (the free service may take about a minute to wake):

[Open the production health endpoint](https://wandermate-fullstack.onrender.com/The-Project/api/v1/health)

## Logs

Render log proof:

![Render logs](../../docs/screenshots/25-render-logs.png)

## Common checks before demo

1. Backend health endpoint works.
2. Login works.
3. Token refresh works.
4. Logout revokes session.
5. Profile/avatar upload works.
6. Trip cover upload works.
7. Owner/editor/viewer roles behave correctly.
8. Backend tests pass.
9. Frontend typecheck passes.
10. Postman protected request works.

## Postman proof

![Postman proof](../../docs/screenshots/24-api-postman-proof.png)

## Security notes

Before exporting or sharing:

- Clear Postman tokens.
- Remove real `.env` files.
- Hide secret values from screenshots.
- Do not include raw database dumps.
