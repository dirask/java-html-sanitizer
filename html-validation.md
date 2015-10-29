# Why sanitize when you can validate?

## Background

A sanitizer takes in a string in a language and puts out a *safe* version.

Occasionally people ask for a function, that instead of returning a
safe version of the input, just labels the input as *safe* or
*unsafe*.

Herein I address why I think the latter is a bad idea for HTML specifically.
Hopefully this will prompt a discussion, and I'm interested why people want
validators.  Please let me know why, and under use-cases maybe we can
definitively answer whether a sanitizer and a validator beats just a sanitizer.

## Defining "Valid"

The sanitizer promises that it's output can be safely embedded in a larger
document.

It seems to me that any *valid* input should also have this property.

#### Valid means idempotent

One na&iuml;ve way to define *valid* is thus:

> A valid input is any input such that `input.equals(sanitized(input))`.

This is sound, but not very useful.  Intuitively, it seems that there
must be a lot of inputs that don't have this property but are not unsafe.

For example, maybe the sanitizer takes as an input

```html
For <a href='//example.com/'>example</a>
```

and returns

```html
For <a href="//example.com/">example</a>
```

This difference seems unimportant.

#### Valid according to policy

Instead, we could try to define *valid* thus:

> An input is valid when the policy rejects no part of it.

This misses part of the picture. A string is safe because of the way
browsers parse it, **not** the way the sanitizer parses it.

```html
<!--if[true]> <script>alert(1337)</script> -->
```

contains a script tag when served to Internet Explorer, but contains no
tags at all when served to other browsers.

If the sanitizer interprets all comments as ignorable content, then
the policy never sees the `<script>` tag, so no policy violation is
recorded.

```html
<![CDATA[ <!-- ]]> <script>alert(1337)</script> <!-- -->
```

contains a `<script>` element when executed in an HTML
[foreign content context](http://www.w3.org/TR/html5/syntax.html#cdata-sections),
but not when included in a normal HTML context.

A string of HTML is not safe because a sanitizer parsed it and found
no policy violations, but because the end users' browsers don't kick
off code-execution when parsing it.

The sanitizer cannot take a position on comments like the above which
is consistent with all the positions that browsers might take.

The sanitizer has to do a lot of work to construct an output that will
be consistently interpreted by browsers

* It drops comments.
* It quoted unquoted attributes
* It normalizes names.
* It even adds spaces to some attribute values for reasons too
  complicated to go into.



#### Valid according to policy++

> An input is valid when the policy rejects no part of it and it is in
> a subset of HTML that all browsers parse the same way.

So valid means safe & uncontroversial -- the most common
interpretation involves no unintended code execution, and widely used
browsers all agree on that interpretation.

The sanitizer does this implicitly -- any output of the sanitizer is in
the safe subset.  Others are not.

We could start with that small safe grammar, and add to it.

* `<b>` is equivalent to `<B>` in many contexts.
* `&nbsp;` is equivalent to a raw U+A0 in many contexts.
* An attribute whose value is non-empty and only alphanumeric does not
  need quotes.
* We can allow whitespace around the `=` sign in an attribute.
* Either double or single quotes can delimit attribute values.
* In URL paths, `+` and `%20` are equivalent.

but each addition expands the number of branches through browsers'
parsers that a sanitized output might trigger.  An attacker only
has to find one input that makes a popular browser branch to
its code interpreter with a payload to win.

We also have to be careful to consider the contexts in which safe
strings can be used.  For example, it is the case that

```java
String safe1 = sanitize("<");
String safe2 = sanitize("scr");
String safe3 = sanitize("ipt");
String safe4 = sanitize(">");
String allSafe = safe1 + safe2 + safe3 + safe4;
```

but it is not the case that

```java
boolean isValid1 = isValid("<");
boolean isValid2 = isValid("scr");
boolean isValid3 = isValid("ipt");
boolean isValid4 = isValid(">");
boolean areAllValid = isValid1 && isValid2 && isValid3 && isValid4;
```

The larger the *valid* grammar, the more we risk shooting ourselves in
our feet by overlooking how *valid* outputs are used.

If we go with a minimal grammar we are back to *valid* <->
*sanitizer is idempotent*.

Maybe there is a happy medium between

> the safe grammar is what the sanitizer outputs

and

> the safe grammar includes strings whose semantics
> depends on who is doing the parsing

but without clear use cases, it is hard to figure out whether there
is a happy middle ground.



## Objections to Validity

### Hard to define *valid* in a way that is useful & usable

As I've argued above, there is a tradeoff between useful & usable
and I haven't seen a concrete proposal that balances these well.

I'm open to concrete proposals though.

### Validity is unstable in the face of emerging threats.

If a new hack is discovered, I can roll out a fix to a sanitizer, clients
update, and now the output of the sanitizer can be safely included in a
larger document.

If a new hack is discovered, I can roll out a fix to a validator that marks
previously valid inputs *invalid*.  What is a client to do?
They could run the sanitizer, but that means that the validator is only
valuable in conjunction with a sanitizer.

### Even if *valid* had a meaning it would not be useful by itself.

I haven't catalogued all the reasons people ask for validity, but I haven't
seen any that don't involve sanitizing at least as a fallback, or that cannot
be implemented by keeping the unsanitized input around.


## Use Cases

### Don't break my heart.

MySpace, an early social networking site, had a lot of users who'd
write HTML like

```html
I <3 Poniez!
```

and would get bug reports when the HTML appeared broken in the browser
and "View Source" showed the `<` changed to `&lt;`.

One use case for validation seems to be to allow an edit window to warn
about markup that would violate a policy.

Knowing that an input is invalid does not help narrow down the
problematic part of the input.

This use case seems to be addressable via

```java
policyThatAllowsEverything.sanitize(input)
   .equals(policy.sanitize(input))
```

It can also be addressed by keeping the unsanitized input around to
repopulate the editor.