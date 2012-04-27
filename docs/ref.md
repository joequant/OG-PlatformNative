[TOC]

{% for k in d['rdoc.R|rdoc'].key_prefixes() -%}
### {{ d['rdoc.R|rdoc']["%s:name" % k] }}

{{ d['rdoc.R|rdoc']["%s:description" % k] }}

<pre>
{{ d['rdoc.R|rdoc']["%s:source" % k] }}
</pre>

{% endfor -%}
