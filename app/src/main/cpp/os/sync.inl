//------------------------------------------------------------------------------
//	File		: sync.inl
//	Path		: include/os/
//	System		: SSP
//	Subsystem	: Synchronization
//	Interface	: include/os/sync.h
//	Description	: Inline Definitions for Synchronization Class
//------------------------------------------------------------------------------
/*=========================================================

  Inline function definitons of class _Mutex

  including:
        _Mutex()
        ~_Mutex()

        wait(void)
        signal(void)

  =========================================================*/
//#define _LINUX
inline _Mutex::_Mutex()
{
#  if defined(_WIN32)
	InitializeCriticalSection(&m_cs);
#  elif defined(_LINUX)
		sem_init ( &m_sema4, 0, 1 );
#  endif
}

inline _Mutex::~_Mutex()
{
#  if defined(_WIN32)
	DeleteCriticalSection(&m_cs);
#  elif defined(_LINUX)
	sem_destroy( &m_sema4 );
#  endif
}

inline void _Mutex::wait(void)
{
#  if defined(_WIN32)
    EnterCriticalSection((LPCRITICAL_SECTION)&m_cs);
#  elif defined(_LINUX)
	sem_wait (&m_sema4);
#  endif
}

inline void _Mutex::signal(void)
{
#  if defined(_WIN32)
	LeaveCriticalSection((LPCRITICAL_SECTION)&m_cs);
#  elif defined(_LINUX)
	sem_post (&m_sema4);
#  endif
}

/*=========================================================

  Inline function definitons of class _InterlockedLong

  including:
        __InterlockedLong(long)()

        operator=(long l)
        operator long() const
        operator++()
        operator++(int)
        operator--()
        operator--(int)

  =========================================================*/

inline _InterlockedLong::_InterlockedLong(long l)
{
#  if defined(_WIN32)
	InterlockedExchange(&m_data, l);
#  elif defined( _LINUX )
	m_mutex.wait();
	m_data = l;
	m_mutex.signal();
#  endif
}

inline _InterlockedLong &_InterlockedLong::operator=(long l)
{
#  if defined(_WIN32)
	InterlockedExchange(&m_data, l);
#  elif defined( _LINUX )
	m_mutex.wait();
	m_data = l;
	m_mutex.signal();
#  endif
	return *this;
}

inline _InterlockedLong::operator long() const
{
	return m_data;
}

inline long _InterlockedLong::operator++()
{
#  if defined(_WIN32)
	InterlockedIncrement(&m_data);
	return m_data;
#  elif defined( _LINUX )
	m_mutex.wait();
	m_data++;
	m_mutex.signal();
	return m_data;
#  endif
}

inline long _InterlockedLong::operator++(int)
{
#  if defined(_WIN32)
	long result = m_data;
	InterlockedIncrement(&m_data);
	return result;
#  elif defined( _LINUX )
	m_mutex.wait();
	long result = m_data;
	m_data++;
	m_mutex.signal();
	return result;
#  endif
}

inline long _InterlockedLong::operator--()
{
#  if defined(_WIN32)
	InterlockedDecrement(&m_data);
    return m_data;
#  elif defined( _LINUX )
	m_mutex.wait();
	m_data--;
	m_mutex.signal();
	return m_data;
#  endif
}

inline long _InterlockedLong::operator--(int)
{
#  if defined(_WIN32)
	long result = m_data;
	InterlockedDecrement(&m_data);
	return result;
#  elif defined( _LINUX )
	m_mutex.wait();
	long result = m_data;
	m_data--;
	m_mutex.signal();
	return result;
#  endif
}

#if defined(_WIN32)
#define MAX_SEMA 0x7ffffff
#endif

inline _Semaphore::_Semaphore(int32 init)
{
#  if defined(_WIN32)
   TCHAR szName[32];
   DWORD dwThreadID  = GetCurrentThreadId();
   wsprintf(szName, _T("EmlSema_%ul_%ul"), dwThreadID, (DWORD)this);
 
   m_Mutex = new _Mutex;
   m_nValue = init;
   m_Semaphore = CreateSemaphore(NULL, init, MAX_SEMA, szName);
#  elif defined(_LINUX)
	sem_init ( &m_sema4, 0, init );
#  endif
}

inline _Semaphore::~_Semaphore()
{
#  if defined(_WIN32)
   if ( m_Mutex!= 0 )
	   delete m_Mutex;
	m_nValue=0;
	CloseHandle(m_Semaphore);
#  elif defined(_LINUX)
	sem_destroy( &m_sema4 );
#  endif
}

inline void _Semaphore::wait(void)
{
#  if defined(_WIN32)
	m_Mutex->wait();
	m_nValue--;
	if( m_nValue < 0 ) {
		m_Mutex->signal();
		WaitForSingleObject(m_Semaphore, INFINITE);
	}
	else
		m_Mutex->signal();
#  elif defined(_LINUX)
	sem_wait (&m_sema4);
#  endif
}

inline void _Semaphore::signal(void)
{
#  if defined(_WIN32)
	m_Mutex->wait();
	m_nValue++;
	if( m_nValue <= 0 ) {
		ReleaseSemaphore(m_Semaphore, 1, NULL);
		m_Mutex->signal(); 
	}
	else
		m_Mutex->signal(); 
#  elif defined(_LINUX)
	sem_post (&m_sema4);
#  endif
}

inline int32 _Semaphore::value(void)
{
#  if defined(_WIN32)
  LONG lCount;
   ReleaseSemaphore(m_Semaphore, 0, &lCount);
  return lCount;
#  elif defined(_LINUX)
	sem_getvalue ( &m_sema4, &m_sema4value );
	return m_sema4value;
#  endif
}
