DESCRIPTION = "Tidy library"
HOMEPAGE = "http://www.clutter-project.org/"
LICENSE = "LGPL"

STDDEPENDS = "clutter-0.8"
BASE_CONF = "--disable-gtk-doc"

DEPENDS = "${STDDEPENDS}"
EXTRA_OECONF = "${BASE_CONF}"

inherit autotools_stage pkgconfig

PACKAGES =+ "tidy-examples"
FILES_tidy-examples = "${bindir}/test-*"

do_configure_prepend() {
        touch ${S}/gtk-doc.make
        sed -i s:docs/reference/Makefile::g ${S}/configure.ac
        sed -i s:docs::g ${S}/Makefile.am
}
