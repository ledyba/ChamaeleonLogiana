/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, psi
 */


#pragma once
#define IS_IOS (defined(__IPHONE_OS_VERSION_MIN_REQUIRED))
#define IS_LINUX (defined(__linux__) || defined(__linux) || defined(__gnu_linux__) || defined(linux))
#define IS_WINDOWS (defined(WIN32) || defined(WIN64) || defined(__WIN32__) || defined(__WIN64__))
#define IS_ANDROID (defined(ANDROID))

#if IS_WINDOWS
#include <windows.h>
#define sleep(n) Sleep(1000 * n)
#endif

#if IS_LINUX

#include <unistd.h>

#endif

