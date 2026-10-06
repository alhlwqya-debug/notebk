# notebk Brand System

The project now treats the visual identity as separate reusable assets:

- `drawable/brand_logo.png` — notebook + AHD application icon/mark.
- `drawable/notebk_wordmark.png` — official `notebk` wordmark with the purple `b`.
- `drawable/developer_logo.png` — AHD developer mark.
- `values/strings.xml` — app name, story line, tagline, AHD DEV TEAM and developer credit.
- `layout/brand_splash.xml` — XML composition using the separated assets.
- `ui/screen/splash/SplashScreen.kt` — live Compose implementation of the same composition.

Latin brand names (`notebk`, `AHD DEV TEAM`) remain LTR inside the RTL Arabic application.
