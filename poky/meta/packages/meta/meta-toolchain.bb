DESCRIPTION = "Meta package for building a installable toolchain"
LICENSE = "MIT"
DEPENDS = "opkg-native opkg-utils-native fakeroot-native sed-native"

inherit meta

SDK_DIR = "${WORKDIR}/sdk"
SDK_OUTPUT = "${SDK_DIR}/image"
SDK_OUTPUT2 = "${SDK_DIR}/image-extras"
SDK_DEPLOY = "${TMPDIR}/deploy/sdk"

IPKG_HOST = "opkg-cl -f ${IPKGCONF_SDK} -o ${SDK_OUTPUT}"
IPKG_TARGET = "opkg-cl -f ${IPKGCONF_TARGET} -o ${SDK_OUTPUT}/${SDK_PREFIX}/${TARGET_SYS}"

TOOLCHAIN_HOST_TASK ?= "task-sdk-host"
TOOLCHAIN_TARGET_TASK ?= "task-poky-standalone-sdk-target task-poky-standalone-sdk-target-dbg"
TOOLCHAIN_OUTPUTNAME ?= "${SDK_NAME}-toolchain-${DISTRO_VERSION}"

RDEPENDS = "${TOOLCHAIN_TARGET_TASK} ${TOOLCHAIN_HOST_TASK}"

do_populate_sdk() {
	rm -rf ${SDK_OUTPUT}
	rm -rf ${SDK_OUTPUT2}
	mkdir -p ${SDK_OUTPUT}
	mkdir -p ${SDK_OUTPUT}${layout_libdir}/opkg/
	mkdir -p ${SDK_OUTPUT}/${SDK_PREFIX}/${TARGET_SYS}${layout_libdir}/opkg/

	rm -f ${IPKGCONF_TARGET}
	touch ${IPKGCONF_TARGET}
	rm -f ${IPKGCONF_SDK}
	touch ${IPKGCONF_SDK}

	package_update_index_ipk
	package_generate_ipkg_conf

	for arch in ${PACKAGE_ARCHS}; do
		revipkgarchs="$arch $revipkgarchs"
	done

	${IPKG_HOST} update
	${IPKG_HOST} install ${TOOLCHAIN_HOST_TASK}

	${IPKG_TARGET} update
	${IPKG_TARGET} install ${TOOLCHAIN_TARGET_TASK}

	install -d ${SDK_OUTPUT}/${SDK_PREFIX}/usr/lib/opkg
	mv ${SDK_OUTPUT}/usr/lib/opkg/* ${SDK_OUTPUT}/${SDK_PREFIX}/usr/lib/opkg/
	rm -Rf ${SDK_OUTPUT}/usr/lib

	install -d ${SDK_OUTPUT}/${SDK_PREFIX}/${TARGET_SYS}/${layout_sysconfdir}
	install -m 0644 ${IPKGCONF_TARGET} ${IPKGCONF_SDK} ${SDK_OUTPUT}/${SDK_PREFIX}/${TARGET_SYS}/${layout_sysconfdir}/

	install -d ${SDK_OUTPUT}/${SDK_PREFIX}/${sysconfdir}
	install -m 0644 ${IPKGCONF_SDK} ${SDK_OUTPUT}/${SDK_PREFIX}/${sysconfdir}/

	# extract and store ipks, pkgdata and shlibs data
	target_pkgs=`cat ${SDK_OUTPUT}/${SDK_PREFIX}/${TARGET_SYS}/usr/lib/opkg/status | grep Package: | cut -f 2 -d ' '`
	mkdir -p ${SDK_OUTPUT2}/${SDK_PREFIX}/ipk/
	mkdir -p ${SDK_OUTPUT2}/${SDK_PREFIX}/pkgdata/runtime/
	mkdir -p ${SDK_OUTPUT2}/${SDK_PREFIX}/${TARGET_SYS}/shlibs/
	for pkg in $target_pkgs ; do
		for arch in $revipkgarchs; do
			pkgnames=${DEPLOY_DIR_IPK}/$arch/${pkg}_*_$arch.ipk
			if [ -e $pkgnames ]; then
				echo "Found $pkgnames"
				cp $pkgnames ${SDK_OUTPUT2}/${SDK_PREFIX}/ipk/
				orig_pkg=`opkg-list-fields $pkgnames | grep OE: | cut -d ' ' -f2`
				pkg_subdir=$arch${TARGET_VENDOR}${@['-' + bb.data.getVar('TARGET_OS', d, 1), ''][bb.data.getVar('TARGET_OS', d, 1) == ('' or 'custom')]}
				mkdir -p ${SDK_OUTPUT2}/${SDK_PREFIX}/pkgdata/$pkg_subdir/runtime
				cp ${TMPDIR}/pkgdata/$pkg_subdir/$orig_pkg ${SDK_OUTPUT2}/${SDK_PREFIX}/pkgdata/$pkg_subdir/
				subpkgs=`cat ${TMPDIR}/pkgdata/$pkg_subdir/$orig_pkg | grep PACKAGES: | cut -b 10-`
				for subpkg in $subpkgs; do
					cp ${TMPDIR}/pkgdata/$pkg_subdir/runtime/$subpkg ${SDK_OUTPUT2}/${SDK_PREFIX}/pkgdata/$pkg_subdir/runtime/
					if [ -e ${TMPDIR}/pkgdata/$pkg_subdir/runtime/$subpkg.packaged ];then
						cp ${TMPDIR}/pkgdata/$pkg_subdir/runtime/$subpkg.packaged ${SDK_OUTPUT2}/${SDK_PREFIX}/pkgdata/$pkg_subdir/runtime/
					fi
					if [ -e ${STAGING_DIR_TARGET}/shlibs/$subpkg.list ]; then
						cp ${STAGING_DIR_TARGET}/shlibs/$subpkg.* ${SDK_OUTPUT2}/${SDK_PREFIX}/${TARGET_SYS}/shlibs/
					fi
				done
				break
			fi
		done
	done

	# Fix or remove broken .la files
	for i in `find ${SDK_OUTPUT}/${SDK_PREFIX}/${TARGET_SYS} -name \*.la`; do
		sed -i 	-e "/^dependency_libs=/s,\([[:space:]']\)${layout_base_libdir},\1${SDK_PREFIX}/${TARGET_SYS}${layout_base_libdir},g" \
			-e "/^dependency_libs=/s,\([[:space:]']\)${layout_libdir},\1${SDK_PREFIX}/${TARGET_SYS}${layout_libdir},g" \
			-e "/^dependency_libs=/s,\-\([LR]\)${layout_base_libdir},-\1${SDK_PREFIX}/${TARGET_SYS}${layout_base_libdir},g" \
			-e "/^dependency_libs=/s,\-\([LR]\)${layout_libdir},-\1${SDK_PREFIX}/${TARGET_SYS}${layout_libdir},g" \
			-e 's/^installed=yes$/installed=no/' $i
	done
	rm -f ${SDK_OUTPUT}/${SDK_PREFIX}/lib/*.la

	# Setup site file for external use
	siteconfig=${SDK_OUTPUT}/${SDK_PREFIX}/site-config
	touch $siteconfig
	for sitefile in ${CONFIG_SITE} ; do
		cat $sitefile >> $siteconfig
	done

	# Create environment setup script
	script=${SDK_OUTPUT}/${SDK_PREFIX}/environment-setup
	touch $script
	echo 'export PATH=${SDK_PREFIX}/bin:$PATH' >> $script
	echo 'export PKG_CONFIG_SYSROOT_DIR=${SDK_PREFIX}/${TARGET_SYS}' >> $script
	echo 'export PKG_CONFIG_PATH=${SDK_PREFIX}/${TARGET_SYS}${layout_libdir}/pkgconfig' >> $script
	echo 'export CONFIG_SITE=${SDK_PREFIX}/site-config' >> $script
	echo 'export CC=${TARGET_PREFIX}gcc' >> $script
	echo 'export CONFIGURE_FLAGS="--target=${TARGET_SYS} --host=${TARGET_SYS}"' >> $script
	if [ "${TARGET_OS}" = "darwin8" ]; then
		echo 'export TARGET_CFLAGS="-I${SDK_PREFIX}/${TARGET_SYS}${layout_includedir}"' >> $script
		echo 'export TARGET_LDFLAGS="-L${SDK_PREFIX}/${TARGET_SYS}${layout_libdir}"' >> $script
		# Workaround darwin toolchain sysroot path problems
		cd ${SDK_OUTPUT}${SDK_PREFIX}/${TARGET_SYS}/usr
		ln -s /usr/local local
	fi
	echo "alias opkg='LD_LIBRARY_PATH=${SDK_PREFIX}/lib ${SDK_PREFIX}/bin/opkg-cl -f ${SDK_PREFIX}/${sysconfdir}/opkg-sdk.conf -o ${SDK_PREFIX}'" >> $script
	echo "alias opkg-target='LD_LIBRARY_PATH=${SDK_PREFIX}/lib ${SDK_PREFIX}/bin/opkg-cl -f ${SDK_PREFIX}/${TARGET_SYS}${layout_sysconfdir}/opkg.conf -o ${SDK_PREFIX}/${TARGET_SYS}'" >> $script

	# Add version information
	versionfile=${SDK_OUTPUT}/${SDK_PREFIX}/version
	touch $versionfile
	echo 'Distro: ${DISTRO}' >> $versionfile
	echo 'Distro Version: ${DISTRO_VERSION}' >> $versionfile
	echo 'Metadata Revision: ${METADATA_REVISION}' >> $versionfile
	echo 'Timestamp: ${DATETIME}' >> $versionfile

	# Package it up
	mkdir -p ${SDK_DEPLOY}
	cd ${SDK_OUTPUT}
	fakeroot tar cfj ${SDK_DEPLOY}/${TOOLCHAIN_OUTPUTNAME}.tar.bz2 .
	cd ${SDK_OUTPUT2}
	fakeroot tar cfj ${SDK_DEPLOY}/${TOOLCHAIN_OUTPUTNAME}-extras.tar.bz2 .
}

do_populate_sdk[nostamp] = "1"
do_populate_sdk[recrdeptask] = "do_package_write"
addtask populate_sdk before do_build after do_install
