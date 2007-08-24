/*
 * Copyright (c) 1997-2003 Erez Zadok
 * Copyright (c) 2001-2003 Stony Brook University
 *
 * For specific licensing information, see the COPYING file distributed with
 * this package, or get one from ftp://ftp.filesystems.org/pub/fist/COPYING.
 *
 * This Copyright notice must be kept intact and distributed with all
 * fistgen sources INCLUDING sources generated by fistgen.
 */
/*
 * Copyright (C) 2004, 2005 Markus Klotzbuecher <mk@creamnet.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or (at your option) any later version.
 */


/*
 *  $Id$
 */

#ifndef __FIST_H_
#define __FIST_H_

/*
 * KERNEL ONLY CODE:
 */
#ifdef __KERNEL__
#include <linux/config.h>
#include <linux/version.h>
#if LINUX_VERSION_CODE < KERNEL_VERSION(2,6,0)
#ifdef CONFIG_MODVERSIONS
# define MODVERSIONS
# include <linux/modversions.h>
#endif /* CONFIG_MODVERSIONS */
#endif /* KERNEL_VERSION < 2.6.0 */
#include <linux/sched.h>
#include <linux/kernel.h>
#include <linux/mm.h>
#include <linux/string.h>
#include <linux/stat.h>
#include <linux/errno.h>
#include <linux/wait.h>
#include <linux/limits.h>
#if LINUX_VERSION_CODE < KERNEL_VERSION(2,6,0)
#include <linux/locks.h>
#else
#include <linux/buffer_head.h>
#include <linux/pagemap.h>
#include <linux/namei.h>
#include <linux/module.h>
#include <linux/mount.h>
#include <linux/page-flags.h>
#include <linux/writeback.h>
#include <linux/statfs.h>
#endif
#include <linux/smp.h>
#include <linux/smp_lock.h>
#include <linux/file.h>
#include <linux/slab.h>
#include <linux/vmalloc.h>
#include <linux/poll.h>
#include <linux/list.h>
#include <linux/init.h>

#if LINUX_VERSION_CODE >= KERNEL_VERSION(2,4,20)
#include <linux/xattr.h>
#endif

#if LINUX_VERSION_CODE >= KERNEL_VERSION(2,6,0)
#include <linux/security.h>
#endif

#include <linux/swap.h>

#include <asm/system.h>
#include <asm/segment.h>
#include <asm/mman.h>
#include <linux/seq_file.h>

/*
 * MACROS:
 */

/* those mapped to ATTR_* were copied from linux/fs.h */
#define FA_MODE		ATTR_MODE
#define FA_UID		ATTR_UID
#define FA_GID		ATTR_GID
#define FA_SIZE		ATTR_SIZE
#define FA_ATIME	ATTR_ATIME
#define FA_MTIME	ATTR_MTIME
#define FA_CTIME	ATTR_CTIME
#define FA_ATIME_SET	ATTR_ATIME_SET
#define FA_MTIME_SET	ATTR_MTIME_SET
#define FA_FORCE	ATTR_FORCE
#define FA_ATTR_FLAGS	ATTR_ATTR_FLAG

/* must be greater than all other ATTR_* flags! */
#define FA_NLINK	2048
#define FA_BLKSIZE	4096
#define FA_BLOCKS	8192
#define FA_TIMES	(FA_ATIME|FA_MTIME|FA_CTIME)
#define FA_ALL		0

/* macros to manage changes between kernels */
#define INODE_DATA(i)	(&(i)->i_data)

#define MIN(x,y) ((x < y) ? (x) : (y))
#define MAX(x,y) ((x > y) ? (x) : (y))
#define MAXPATHLEN PATH_MAX

#if LINUX_VERSION_CODE < KERNEL_VERSION(2,4,5)
# define lookup_one_len(a,b,c) lookup_one(a,b)
#endif /* LINUX_VERSION_CODE < KERNEL_VERSION(2,4,5) */

#if LINUX_VERSION_CODE < KERNEL_VERSION(2,4,8)
# define generic_file_llseek default_llseek
#endif /* LINUX_VERSION_CODE < KERNEL_VERSION(2,4,8) */

#ifndef SEEK_SET
# define SEEK_SET 0
#endif /* not SEEK_SET */

#ifndef SEEK_CUR
# define SEEK_CUR 1
#endif /* not SEEK_CUR */

#ifndef SEEK_END
# define SEEK_END 2
#endif /* not SEEK_END */

#ifndef DEFAULT_POLLMASK
# define DEFAULT_POLLMASK (POLLIN | POLLOUT | POLLRDNORM | POLLWRNORM)
#endif /* not DEFAULT_POLLMASK */

/* XXX: fix this so fistgen generates kfree() code directly */
#define kfree_s(a,b) kfree(a)

/*
 * TYPEDEFS:
 */
typedef struct dentry dentry_t;
typedef struct file file_t;
typedef struct inode inode_t;
typedef inode_t vnode_t;
typedef struct page page_t;
typedef struct qstr qstr_t;
typedef struct super_block super_block_t;
typedef super_block_t vfs_t;
typedef struct vm_area_struct vm_area_t;


/*
 * EXTERNALS:
 */

#define FPPF(str,page) printk("PPF %s 0x%x/%d: Lck:%d Err:%d Ref:%d Upd:%d Other::%d:%d:%d:%d:\n", \
		str, \
		(int) page, \
		(int) page->index, \
		(PageLocked(page) ? 1 : 0), \
		(PageError(page) ? 1 : 0), \
		(PageReferenced(page) ? 1 : 0), \
		(Page_Uptodate(page) ? 1 : 0), \
		(PageDecrAfter(page) ? 1 : 0), \
		(PageSlab(page) ? 1 : 0), \
		(PageSwapCache(page) ? 1 : 0), \
		(PageReserved(page) ? 1 : 0) \
		)
#define EZKDBG printk("EZK %s:%d:%s\n",__FILE__,__LINE__,__FUNCTION__)
#if 0
# define EZKDBG1 printk("EZK %s:%d\n",__FILE__,__LINE__)
#else
# define EZKDBG1
#endif

extern int fist_get_debug_value(void);
extern int fist_set_debug_value(int val);
#if 0 /* mini_fo doesn't need these */
extern void fist_dprint_internal(int level, char *str,...);
extern void fist_print_dentry(char *str, const dentry_t *dentry);
extern void fist_print_inode(char *str, const inode_t *inode);
extern void fist_print_file(char *str, const file_t *file);
extern void fist_print_buffer_flags(char *str, struct buffer_head *buffer);
extern void fist_print_page_flags(char *str, page_t *page);
extern void fist_print_page_bytes(char *str, page_t *page);
extern void fist_print_pte_flags(char *str, const page_t *page);
extern void fist_checkinode(inode_t *inode, char *msg);
extern void fist_print_sb(char *str, const super_block_t *sb);

/* §$% by mk: special debug functions */
extern void fist_mk_print_dentry(char *str, const dentry_t *dentry);
extern void fist_mk_print_inode(char *str, const inode_t *inode);

extern char *add_indent(void);
extern char *del_indent(void);
#endif/* mini_fo doesn't need these */


#define STATIC
#define ASSERT(EX)	\
do {	\
    if (!(EX)) {	\
	printk(KERN_CRIT "ASSERTION FAILED: %s at %s:%d (%s)\n", #EX,	\
	       __FILE__, __LINE__, __FUNCTION__);	\
	(*((char *)0))=0;	\
    }	\
} while (0)
/* same ASSERT, but tell me who was the caller of the function */
#define ASSERT2(EX)	\
do {	\
    if (!(EX)) {	\
	printk(KERN_CRIT "ASSERTION FAILED (caller): %s at %s:%d (%s)\n", #EX,	\
	       file, line, func);	\
	(*((char *)0))=0;	\
    }	\
} while (0)

#if 0 /* mini_fo doesn't need these */
#define dprintk(format, args...) printk(KERN_DEBUG format, ##args)
#define fist_dprint(level, str, args...) fist_dprint_internal(level, KERN_DEBUG str, ## args)
#define print_entry_location() fist_dprint(4, "%sIN:  %s %s:%d\n", add_indent(), __FUNCTION__, __FILE__, __LINE__)
#define print_exit_location() fist_dprint(4, "%s OUT: %s %s:%d\n", del_indent(), __FUNCTION__, __FILE__, __LINE__)
#define print_exit_status(status) fist_dprint(4, "%s OUT: %s %s:%d, STATUS: %d\n", del_indent(), __FUNCTION__, __FILE__, __LINE__, status)
#define print_exit_pointer(status) \
do { \
  if (IS_ERR(status)) \
    fist_dprint(4, "%s OUT: %s %s:%d, RESULT: %ld\n", del_indent(), __FUNCTION__, __FILE__, __LINE__, PTR_ERR(status)); \
  else \
    fist_dprint(4, "%s OUT: %s %s:%d, RESULT: 0x%x\n", del_indent(), __FUNCTION__, __FILE__, __LINE__, PTR_ERR(status)); \
} while (0)
#endif/* mini_fo doesn't need these */

#endif /* __KERNEL__ */


/*
 * DEFINITIONS FOR USER AND KERNEL CODE:
 * (Note: ioctl numbers 1--9 are reserved for fistgen, the rest
 *  are auto-generated automatically based on the user's .fist file.)
 */
# define FIST_IOCTL_GET_DEBUG_VALUE	_IOR(0x15, 1, int)
# define FIST_IOCTL_SET_DEBUG_VALUE	_IOW(0x15, 2, int)

#endif /* not __FIST_H_ */