name := "finagle-docs"

enablePlugins(SphinxPlugin, PreprocessPlugin)

ghpages.settings
git.remoteRepo := "git@github.com:gutefrage/the-finagle-docs.git"

sourceDirectory in Sphinx := baseDirectory.value / "docs"


