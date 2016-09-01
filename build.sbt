lazy val docs = project.in(file("."))
  .enablePlugins(SphinxPlugin, PreprocessPlugin /*, ParadoxSitePlugin*/)
  .settings(
    name := "finagle-docs",
    sourceDirectory in Sphinx := baseDirectory.value / "docs"
  )
