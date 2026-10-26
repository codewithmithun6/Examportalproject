# Examportalproject
examfront UI and examserver backend project and notes

## Email-Verified Registration

The public registration flow sends a six-digit code to the submitted email address. The account is created with the `NORMAL` role only after the code is verified. Codes expire after 10 minutes and allow up to five verification attempts. The pending registration is held in server memory, so a server restart clears it and the user must request a new code.

Configure SMTP before starting `examServer`. For Gmail, use an app password rather than the account password. Set these environment variables in the shell that launches the backend:

```powershell
$env:MAIL_USERNAME = "your-sending-address@gmail.com"
$env:MAIL_APP_PASSWORD = "your-16-character-app-password"
```

Optional overrides are `MAIL_HOST` (default `smtp.gmail.com`) and `MAIL_PORT` (default `587`). Do not commit real mail credentials to this repository. If SMTP is not configured, registration requests receive a service-unavailable response and no account is created.
