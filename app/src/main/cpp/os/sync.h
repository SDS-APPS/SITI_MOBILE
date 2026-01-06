#ifndef _SYNC_H
#define _SYNC_H
//#define _LINUX

#if defined(_WIN32)
#if defined (_MFC)
	#include <stdafx.h>
#else
	#include <windows.h>
#endif
  #include <tchar.h>
#elif defined(_IIIOS)
  #include <sys/syscall.h>
  #include <sys/msg.h>
#elif defined(_TICKOS)
  #include <sys/syscall.h>
#elif defined(_SUPERTASK)
  #include "mtcfg.h"    // configuration of MT!(includes "depends.h")
  #include "mtlib.h"    // function prototypes of MT!
  #include "mtsys.h"
  #include "dlist.h"
#elif defined(_LINUX)
  #include <semaphore.h>
#endif

#include "../typedef.h"
//added by Cligon for printf()
#include <stdio.h>


class _Mutex
{
// Constructor & destructor
public:
	_Mutex();
	~_Mutex();

// Methods
public:
	void wait(void);
	void signal(void);
	
private:
#ifndef _SINGLETHREAD
  #if defined(_WIN32)
	CRITICAL_SECTION m_cs;
  #elif defined(_IIIOS) || defined(_TICKOS)
	int m_sema;
  #elif defined(_SUPERTASK)
	word m_wResID;
  #elif defined(_LINUX)
	sem_t m_sema4;
  #else
	#error undefine platform symbol
  #endif
#endif
};

class _InterlockedLong
{
// Constructor & destructor
public:
	_InterlockedLong(long l = 0L);

// Operators
public:
    _InterlockedLong &operator=(long l);
	operator long() const;
	long operator++();
	long operator++(int);
	long operator--();
	long operator--(int);
// Variables
private:
	long m_data;
#ifndef _SINGLETHREAD
  #if defined(_IIIOS) || defined(_TICKOS) || defined(_SUPERTASK) || defined( _LINUX )
	_Mutex m_mutex;
  #endif
#endif
};

class _Semaphore
{
// Variables
private:
#ifndef _SINGLETHREAD
#  if defined(_TICKOS) || defined(_IIIOS)
	int m_sema;
#  elif defined(_SUPERTASK)
// property
	int32 m_nValue;
// objects for internal use
	_Mutex m_Mutex;
    	DList<byte> m_listTask;
#  elif defined(_WIN32)
   	HANDLE m_Semaphore;
// property
	int32 m_nValue;
// objects for internal use
	_Mutex *m_Mutex;
# elif defined(_LINUX)
	int m_sema4value;
	sem_t m_sema4;
#  else
#    error _Semaphore do not support this platform
#  endif
#endif

// Constructor & Destructor
public:
	_Semaphore(int32 init);
	~_Semaphore();

// Methods
public:
	void wait(void);
	void signal(void);
	int32 value(void);
};

#include "sync.inl"

#endif
