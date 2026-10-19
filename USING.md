## Using

### Purpose

The resource extension allows you to retrieve the contents of files that are bundled with a model. Bundled resources,
which were added in NetLogo 7, provide a way to use the contents of other files in a model without needing to send the
files alongside the model. When you add a resource to a model, the contents of the source file are stored in the model
file.

For details on bundled resources, see [Resource Manager](/resource-manager).

### Getting Started

The resource extension comes pre-installed with NetLogo. To use the resource extension in your model,
add a line to the top of your Code tab:

```
extensions [resource]
```

If your model already uses other extensions, then it already has an `extensions` line in it,
so just add `resource` to the list.

For more information on using NetLogo extensions, see the
[Extensions Guide](http://ccl.northwestern.edu/netlogo/docs/extensions.html)
