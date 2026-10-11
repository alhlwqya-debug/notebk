# إعداد تسجيل الدخول في notebk عبر Supabase وGoogle

## عنوان إعادة التوجيه الخاص بالتطبيق

استخدم العنوان التالي في قائمة Redirect URLs داخل Supabase Authentication → URL Configuration:

`notebk://auth/callback`

تسجيل Android لهذا الرابط موجود في `app/src/main/AndroidManifest.xml` على فرع `feature/supabase-google-auth`.

## إعداد Google

1. افتح Supabase Dashboard → Authentication → Sign In / Providers → Google.
2. فعّل Google وأدخل بيانات OAuth الخاصة بتطبيق الويب من Google Cloud.
3. يجب أن يكون عنوان callback لدى Google هو عنوان Supabase الذي يظهر في صفحة مزود Google، بصيغة `https://<project-ref>.supabase.co/auth/v1/callback`؛ لا تستخدم رابط التطبيق هنا.
4. أضف `notebk://auth/callback` إلى قائمة Redirect URLs في Supabase.
5. لا تضع Google Client Secret أو كلمة مرور PostgreSQL أو service_role key في تطبيق Android أو GitHub. بما أن السر ظهر في لقطة شاشة سابقة، أعد تدويره من Google Cloud ثم حدّث Supabase.

## ملاحظات الأمان والمزامنة

- استخدم Publishable Key فقط في عميل Android.
- فعّل RLS على كل جدول سحابي، واجعل سياسات القراءة والكتابة مقيدة بـ `auth.uid()` للمستخدم الحالي.
- لا تعتبر تسجيل الدخول وحده ضمانًا لعزل السجلات؛ يجب فحص سياسات RLS ومخطط الجداول قبل اعتماد المزامنة.
- احتفظ بقاعدة البيانات المحلية وخيار الضيف؛ لا تمسح سجلات الجهاز عند إضافة Google OAuth.
- يجب اختبار البناء وتسجيل الدخول والعودة من المتصفح والمزامنة على جهاز فعلي قبل نشر الإصدار.
