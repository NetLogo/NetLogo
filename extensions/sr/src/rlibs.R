# for reasons beyond my comprehension, loading the rjson package immediately after insalling it in the same R session
# does not work.  So... just install it in a separate script and then the real script can use it just fine. -Jeremy B
# April 2022

# if a user doesn't have write access to their R install location, typical on Windows, we need a fallback.  We don't
# really want to mess with their actual R stuff, though, so we'll place the package needed for this extension inside the
# NetLogo extension's user directory.  That'll keep it separated and not cause trouble with anything else the user is
# doing with R.  -Jeremy B April 2022

rExtensionUserDirPath <- commandArgs(trailingOnly = TRUE)[1]
rExtensionLibraryPath <- file.path(rExtensionUserDirPath, paste0("r-", R.version$major, ".", R.version$minor, "-library"))

.libPaths(c(.libPaths(), rExtensionLibraryPath))

if (!suppressPackageStartupMessages(require("rjson"))) {
  if (!dir.exists(rExtensionLibraryPath)) {
    dir.create(rExtensionLibraryPath)
  }
  suppressPackageStartupMessages(
    install.packages(
      "rjson"
    , repos = "https://cran.case.edu/"
    , quiet = TRUE
    , lib = rExtensionLibraryPath
    )
  )
}
