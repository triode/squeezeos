FEED_ARCH = "armv5te"

TARGET_CC_ARCH = "-march=armv5te -mtune=xscale"
TARGET_CC_KERNEL_ARCH = "-march=armv5te -mtune=xscale"
BASE_PACKAGE_ARCH = "${@['armv5teb', 'armv5te'][bb.data.getVar('SITEINFO_ENDIANESS', d, 1) == 'le']}"

# webkit-gtk has alignment issues with double instructions on armv5 so
# disable them here
TARGET_CC_ARCH_pn-webkit-gtk = "-march=armv4t"
TARGET_CC_ARCH_pn-cairo = "-march=armv4t"
