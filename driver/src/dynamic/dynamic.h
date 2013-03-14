/* coding: utf-8 */
/**
 * Logiana
 *
 * Copyright 2013, psi
 */


#pragma once
#include "../device/Util.h"

#if IS_WINDOWS

#if defined(DLL_EXPORT)
#define DYNAPI __declspec(dllexport)
#else
#define DYNAPI __declspec(dllimport)
#endif

#else
#define DYNAPI
#endif

#ifdef __cplusplus
extern "C" {
#endif

DYNAPI extern int findLogiana(void** ptr, char* err, int errlen);
DYNAPI extern int nowLogianaMeasuring(void* device, char* err, int errlen);
DYNAPI extern int startMeasuring(void* device, char* err, int errlen, char freq, char mesType, char cond, char line );
DYNAPI extern int endMeasuring(void* device, char* err, int errlen, unsigned int* buffer, int buflen);
DYNAPI extern void closeLogiana(void* device);

#ifdef __cplusplus
}
#endif
