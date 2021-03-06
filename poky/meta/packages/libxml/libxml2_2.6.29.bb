require libxml2.inc

PR = "r7"

export LDFLAGS += "-ldl"

do_stage() {
	autotools_stage_all
	install -d ${STAGING_DATADIR}/aclocal/
	install -d ${STAGING_BINDIR_CROSS}

 	install -m 0644 libxml.m4 ${STAGING_DATADIR}/aclocal/
	#this is need it by php during its install
	install -m 0755 xml2-config ${STAGING_BINDIR_CROSS}
}

python populate_packages_prepend () {
	# autonamer would call this libxml2-2, but we don't want that
	if bb.data.getVar('DEBIAN_NAMES', d, 1):
		bb.data.setVar('PKG_libxml2', 'libxml2', d)
}

PACKAGES = "${PN}-dbg ${PN}-dev ${PN}-utils ${PN} ${PN}-doc ${PN}-locale"

FILES_${PN}-dev += "${bindir}/*-config"
FILES_${PN}-utils += "${bindir}/*"
