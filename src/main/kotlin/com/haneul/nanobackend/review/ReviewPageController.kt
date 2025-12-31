package com.haneul.nanobackend.review

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ReviewPagesController {

    @GetMapping("/review", produces = [MediaType.TEXT_HTML_VALUE])
    fun home(): String = """
        <!doctype html>
        <html>
          <head>
            <meta charset="utf-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1"/>
            <title>nano2 – Meta App Review</title>
            <style>
              body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; padding: 24px; }
              .card { border: 1px solid #ddd; border-radius: 12px; padding: 16px; margin: 12px 0; }
              a.button { display: inline-block; padding: 10px 14px; border-radius: 10px; border: 1px solid #333; text-decoration: none; }
              code { background: #f6f6f6; padding: 2px 6px; border-radius: 6px; }
            </style>
          </head>
          <body>
            <h1>nano2 – Meta App Review pages</h1>
            <p>Use the links below to verify requested permissions.</p>

            <div class="card">
              <h3>Step 1) Connect with Facebook</h3>
              <a class="button" href="/oauth2/authorization/meta">Connect (OAuth Login)</a>
              <p style="margin-top:10px; color:#666;">If you are not logged in, clicking Pages/Instagram will also redirect you to login.</p>
            </div>

            <div class="card">
              <h3>Permission: pages_show_list</h3>
              <p>Shows pages available to the user via <code>/me/accounts</code>.</p>
              <a class="button" href="/review/pages">Open Pages Permission Page</a>
            </div>

            <div class="card">
              <h3>Permission: instagram_basic (+ pages perms)</h3>
              <p>Resolves IG business account from a Page and fetches IG profile fields.</p>
              <a class="button" href="/review/instagram">Open Instagram Permission Page</a>
            </div>
          </body>
        </html>
    """.trimIndent()

    @GetMapping("/review/pages", produces = [MediaType.TEXT_HTML_VALUE])
    fun pages(): String = """
        <!doctype html>
        <html>
          <head>
            <meta charset="utf-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1"/>
            <title>Permission Review – pages_show_list</title>
            <style>
              body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; padding: 24px; }
              pre { background:#0b1020; color:#d7e2ff; padding:16px; border-radius:12px; overflow:auto; }
              a.button { display: inline-block; padding: 10px 14px; border-radius: 10px; border: 1px solid #333; text-decoration: none; }
            </style>
          </head>
          <body>
            <h1>Permission: pages_show_list</h1>
            <p>This page calls <code>/api/meta/pages</code>, which calls Graph <code>/me/accounts?fields=id,name,access_token</code>.</p>

            <p>
              <a class="button" href="/oauth2/authorization/meta">Login / Re-auth</a>
              <a class="button" href="/review">Back</a>
            </p>

            <h3>Result</h3>
            <pre id="out">Loading…</pre>

            <script>
              fetch('/api/meta/pages')
                .then(async r => {
                  if (!r.ok) throw new Error('HTTP ' + r.status + ': ' + await r.text());
                  return r.json();
                })
                .then(data => {
                  document.getElementById('out').textContent = JSON.stringify(data, null, 2);
                })
                .catch(err => {
                  document.getElementById('out').textContent = String(err);
                });
            </script>
          </body>
        </html>
    """.trimIndent()

    @GetMapping("/review/instagram", produces = [MediaType.TEXT_HTML_VALUE])
    fun instagram(): String = """
        <!doctype html>
        <html>
          <head>
            <meta charset="utf-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1"/>
            <title>Permission Review – instagram_basic</title>
            <style>
              body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; padding: 24px; }
              pre { background:#0b1020; color:#d7e2ff; padding:16px; border-radius:12px; overflow:auto; }
              a.button { display: inline-block; padding: 10px 14px; border-radius: 10px; border: 1px solid #333; text-decoration: none; }
            </style>
          </head>
          <body>
            <h1>Permission: instagram_basic (+ pages perms)</h1>
            <p>This page calls <code>/api/meta/instagram</code> and runs:</p>
            <ol>
              <li><code>/me/accounts</code> → pick first page</li>
              <li><code>/{pageId}?fields=instagram_business_account</code></li>
              <li><code>/{igUserId}?fields=id,username,media_count</code></li>
            </ol>

            <p>
              <a class="button" href="/oauth2/authorization/meta">Login / Re-auth</a>
              <a class="button" href="/review">Back</a>
            </p>

            <h3>Result</h3>
            <pre id="out">Loading…</pre>

            <script>
              fetch('/api/meta/instagram')
                .then(async r => {
                  if (!r.ok) throw new Error('HTTP ' + r.status + ': ' + await r.text());
                  return r.json();
                })
                .then(data => {
                  document.getElementById('out').textContent = JSON.stringify(data, null, 2);
                })
                .catch(err => {
                  document.getElementById('out').textContent = String(err);
                });
            </script>
          </body>
        </html>
    """.trimIndent()
}
