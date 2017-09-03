name := "finagle-docs"

enablePlugins(MicrositesPlugin, PreprocessPlugin)

// microsite configuration
micrositeName := "The Finagle Docs"
micrositeAuthor := "Gutefrage.net GmbH"
micrositeDescription := "A practical guide for Finagle"
micrositeHomepage := "https://gutefrage.github.io/the-finagle-docs"
micrositeOrganizationHomepage := "https://www.gutefrage.net"
micrositeTwitter := "@gutefrageIT"
micrositeTwitterCreator := "@gutefrageIT"
micrositeGithubOwner := "gutefrage"
micrositeGithubRepo := "the-finagle-docs"
micrositeHighlightTheme := "atom-one-light"
micrositeGitterChannel := false
// micrositeAnalyticsToken := "UA-XXXX-Y"
micrositePalette := Map(
  // primary and secondary color are swapped
  "brand-primary" -> "#4DB8AF",
  "brand-secondary" -> "#49A0CC",
  "brand-tertiary" -> "#222749",
  "gray-dark" -> "#646767",
  "gray" -> "#A8ACAD",
  "gray-light" -> "#CACDCD",
  "gray-lighter" -> "#EDEEEE",
  "white-color" -> "#FFFFFF"
)

// ghpages.settings
// git.remoteRepo := "git@github.com:gutefrage/the-finagle-docs.git"
