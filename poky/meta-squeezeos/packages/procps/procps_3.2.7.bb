require procps.inc

PR = "r6"

SRC_URI += "file://procmodule.patch;patch=1 \
            file://psmodule.patch;patch=1 \
	    file://linux-limits.patch;patch=1"

EXTRA_OEMAKE = "CFLAGS=-I${STAGING_INCDIR} \
		CPPFLAGS=-I${STAGING_INCDIR} \
                LDFLAGS=-L${STAGING_LIBDIR} -Wl,--rpath-link,${STAGING_LIBDIR} \
                CURSES=-lncurses \
                install='install -D' \
                ldconfig=echo"

do_install_append () {
	#mv ${D}${bindir}/uptime ${D}${bindir}/uptime.${PN}
	#mv ${D}${bindir}/top ${D}${bindir}/top.${PN}
	#mv ${D}${base_bindir}/kill ${D}${base_bindir}/kill.${PN}
	mv ${D}${base_bindir}/ps ${D}${base_bindir}/ps.${PN}
	#mv ${D}${bindir}/free ${D}${bindir}/free.${PN}
	#mv ${D}${base_sbindir}/sysctl ${D}${base_sbindir}/sysctl.${PN}
}

pkg_postinst() {
	#update-alternatives --install ${bindir}/top top top.${PN} 90
	#update-alternatives --install ${bindir}/uptime uptime uptime.${PN} 90
	update-alternatives --install ${base_bindir}/ps ps ps.${PN} 90
	#update-alternatives --install ${base_bindir}/kill kill kill.${PN} 90
	#update-alternatives --install ${bindir}/free free free.${PN} 90
	#update-alternatives --install ${base_sbindir}/sysctl sysctl sysctl.${PN} 90
}

pkg_postrm() {
	#update-alternatives --remove top top.${PN}
	update-alternatives --remove ps ps.${PN}
	#update-alternatives --remove uptime uptime.${PN}
	#update-alternatives --remove kill kill.${PN}
	#update-alternatives --remove free free.${PN}
	#update-alternatives --remove sysctl sysctl.${PN}
}


PACKAGES = "${PN}-dbg ${PN}-doc ${PN}"

FILES_${PN} = "${base_libdir}"
FILES_${PN} += "${base_bindir}/ps.${PN}"
FILES_${PN}-doc = "${mandir}