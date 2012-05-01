[TOC]

{% for k in d['rdoc.R|rdoc'].key_prefixes() -%}
### {{ d['rdoc.R|rdoc']["%s:name" % k] }}

{{ d['rdoc.R|rdoc']["%s:description" % k] }}

<table>
{% for k, v in json.loads(d['rdoc.R|rdoc'].storage().get("%s:arguments" % k, "{}")).iteritems() -%}
<tr><th>{{ k }}</th><td>{{ v }}</td></tr>
{% endfor -%}
</table>

<pre>
{{ d['rdoc.R|rdoc']["%s:source" % k] }}
</pre>

{% endfor -%}
