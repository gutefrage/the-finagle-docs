lazy val docs = project.in(file("."))
  .enablePlugins(SphinxPlugin, PreprocessPlugin /*, ParadoxSitePlugin*/)
  .settings(ghpages.settings)
  .settings(
    name := "finagle-docs",
    sourceDirectory in Sphinx := baseDirectory.value / "docs",
    git.remoteRepo := "git@github.com:gutefrage/the-finagle-docs.git"
  )
