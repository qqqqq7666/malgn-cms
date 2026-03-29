import React, { useState, useEffect } from 'react';
import { User, LogOut, Edit, Trash2, Plus, ArrowLeft, Search, Eye, Clock, RotateCcw, Timer, RefreshCw, ShieldCheck, AlertCircle } from 'lucide-react';

// ==========================================
// 백엔드 API 주소 설정
// ==========================================
const API_BASE = 'https://kcjin-malgn-cms.o-r.kr/api/v1';

// ==========================================
// 메인 App 컴포넌트
// ==========================================
export default function App() {
  // --- 상태 관리 ---
  const [view, setView] = useState('list'); // list, detail, form, login, signup
  const [auth, setAuth] = useState(() => {
    const savedToken = localStorage.getItem('accessToken');
    const savedUsername = localStorage.getItem('username');
    return savedToken ? { token: savedToken, username: savedUsername } : { token: null, username: null };
  });
  // 토큰 만료 시간 상태 (타임스탬프 저장)
  const [tokenExpirations, setTokenExpirations] = useState(() =>{
    const access = localStorage.getItem('accessExp');
    const refresh = null;
    return {
      // localStorage는 모든 값을 '문자열'로 저장하므로, 숫자로 변환해주면 좋습니다.
      access: access ? parseInt(access, 10) : null,
      refresh: refresh
    };
  });

  const [contents, setContents] = useState([]);
  const [selectedContent, setSelectedContent] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 검색 조건 상태 추가
  const [searchParams, setSearchParams] = useState({ type: 'title', keyword: '' });

  // --- API 호출 공통 함수 ---
  const apiFetch = async (endpoint, options = {}) => {
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    // Access Token이 있으면 Authorization 헤더에 추가
    if (auth.token) {
      headers['Authorization'] = `Bearer ${auth.token}`;
    }

    // Refresh Token 쿠키를 주고받기 위해 credentials 옵션 추가 (필수)
    const fetchOptions = {
      ...options,
      headers,
      credentials: 'include',
    };

    try {
      const response = await fetch(`${API_BASE}${endpoint}`, fetchOptions);

      // 204 No Content 처리 (삭제 시 등)
      if (response.status === 204) return null;

      const data = await response.json();

      // 백엔드의 ApiResponse 규격 (success 플래그) 확인
      if (!response.ok || data.success === false) {
        throw new Error(data.error?.message || data.message || 'API 요청에 실패했습니다.');
      }

      return data.data || data; // 백엔드 응답 구조에 맞게 유연하게 반환
    } catch (error) {
      console.error('API 통신 에러:', error);
      throw error;
    }
  };

  // --- 컨텐츠 목록 조회 (검색 기능 포함) ---
  const fetchContents = async (currentSearchParams = searchParams) => {
    setLoading(true);
    setError('');
    try {
      let endpoint = '/contents';

      // 검색어가 존재할 경우 검색 API(/contents/search) 호출
      if (currentSearchParams.keyword) {
        const params = new URLSearchParams();
        params.append(currentSearchParams.type, currentSearchParams.keyword);
        endpoint = `/contents/search?${params.toString()}`;
      }

      const data = await apiFetch(endpoint);
      setContents(data.content || data || []);
    } catch (err) {
      setError('목록을 불러오는 중 오류가 발생했습니다.');
      setContents([]);
    } finally {
      setLoading(false);
    }
  };

  // --- 컨텐츠 상세 조회 ---
  const fetchContentDetail = async (id) => {
    setLoading(true);
    setError('');
    try {
      const data = await apiFetch(`/contents/${id}`);
      setSelectedContent(data);
      setView('detail');
    } catch (err) {
      setError('상세 내용을 불러오는 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 화면이 'list'로 바뀔 때마다 목록 갱신 (검색 상태 유지)
  useEffect(() => {
    if (view === 'list') {
      fetchContents();
      setSelectedContent(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [view]);

  // --- 공통: 로그아웃 처리 ---
  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('username');
    localStorage.removeItem('accessExp');
    setAuth({ token: null, username: null });
    setTokenExpirations({ access: null, refresh: null });
    setView('list');
  };

  // --- 컴포넌트들 ---

  const Header = () => (
    <header className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-10">
      <div className="max-w-5xl mx-auto px-4 h-16 flex items-center justify-between">
        <div className="flex items-center space-x-2 cursor-pointer" onClick={() => setView('list')}>
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold text-xl">
            M
          </div>
          <span className="text-xl font-bold text-gray-900 tracking-tight">Malgn CMS</span>
        </div>
        <div className="flex items-center space-x-4">
          {auth.token ? (
            <div className="flex items-center space-x-4">
              <span className="text-sm font-medium text-gray-600 flex items-center">
                <User className="w-4 h-4 mr-1" /> {auth.username}님
              </span>
              <button
                onClick={handleLogout}
                className="text-sm text-gray-500 hover:text-gray-800 flex items-center transition-colors"
              >
                <LogOut className="w-4 h-4 mr-1" /> 로그아웃
              </button>
            </div>
          ) : (
            <div className="space-x-2">
              <button onClick={() => setView('login')} className="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg transition-colors">
                로그인
              </button>
              <button onClick={() => setView('signup')} className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors shadow-sm">
                회원가입
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );

  // --- 토큰 타이머 및 Reissue 관리 배너 ---
  const TokenManagerBanner = () => {
    const [now, setNow] = useState(Date.now());
    const [isReissuing, setIsReissuing] = useState(false);

    // 1초마다 현재 시간 업데이트
    useEffect(() => {
      if (!auth.token) return;
      const interval = setInterval(() => setNow(Date.now()), 1000);
      return () => clearInterval(interval);
    }, [auth.token]);

    if (!auth.token) return null;

    // 밀리초를 hh:mm:ss 로 포맷팅
    const formatTimeLeft = (expirationTime) => {
      if (!expirationTime) return '--:--';
      const diff = Math.floor((expirationTime - now) / 1000);

      if (diff <= 0) return '만료됨';

      const d = Math.floor(diff / (24 * 3600));
      const h = Math.floor((diff % (24 * 3600)) / 3600);
      const m = Math.floor((diff % 3600) / 60).toString().padStart(2, '0');
      const s = (diff % 60).toString().padStart(2, '0');

      if (d > 0) return `${d}일 ${h}시간 ${m}분 ${s}초`;
      if (h > 0) return `${h}시간 ${m}분 ${s}초`;
      return `${m}분 ${s}초`;
    };

    const isAccessExpired = tokenExpirations.access && (tokenExpirations.access - now <= 0);

    const handleReissue = async () => {
      setIsReissuing(true);
      try {
        const data = await apiFetch('/auth/reissue', { method: 'POST', credentials: 'include'});

        // Reissue 성공 시 새로운 토큰 시간 적용
        // accessTokenExpiration 은 서버에서 long 값(예: 3600000 = 1시간)으로 반환한다고 가정
        const accessExpDuration = data.accessTokenExpiration * 1000 || 3600000;

        setTokenExpirations({
          access: Date.now() + accessExpDuration,
        });
        setAuth(prev => ({ ...prev, token: data.accessToken }))
        localStorage.setItem('accessExp', Date.now() + data.accessTokenExpiration * 1000);

      } catch (err) {
        alert('토큰 갱신에 실패했습니다. 다시 로그인해주세요.\n' + err.message);
        handleLogout();
      } finally {
        setIsReissuing(false);
      }
    };

    return (
      <div className={`border-b px-4 py-2.5 flex items-center justify-between text-sm transition-colors ${isAccessExpired ? 'bg-red-50 border-red-200' : 'bg-indigo-50 border-indigo-100'}`}>
        <div className="max-w-5xl mx-auto w-full flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center space-x-6">
            <div className="flex items-center font-medium text-indigo-900">
              <ShieldCheck className="w-4 h-4 mr-1.5 text-indigo-600" />
              보안 세션 상태
            </div>

            <div className="flex items-center space-x-4">
              {/* Access Token 타이머 */}
              <div className={`flex items-center space-x-1.5 ${isAccessExpired ? 'text-red-600 font-bold' : 'text-gray-700'}`}>
                <Timer className="w-4 h-4" />
                <span>Access:</span>
                <span className="tabular-nums font-mono">{formatTimeLeft(tokenExpirations.access)}</span>
              </div>
            </div>
          </div>

          <div className="flex items-center">
            {isAccessExpired && (
              <span className="flex items-center text-red-600 mr-3 text-xs font-semibold">
                <AlertCircle className="w-3.5 h-3.5 mr-1" />
                토큰 만료됨 (갱신 필요)
              </span>
            )}
            <button
              onClick={handleReissue}
              disabled={isReissuing}
              className={`flex items-center px-3 py-1.5 text-xs font-medium rounded-md transition-all shadow-sm
                ${isReissuing
                ? 'bg-indigo-200 text-indigo-500 cursor-not-allowed'
                : 'bg-indigo-600 text-white hover:bg-indigo-700 active:scale-95'}`}
            >
              <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${isReissuing ? 'animate-spin' : ''}`} />
              {isReissuing ? '갱신 중...' : '토큰 연장하기 (Reissue)'}
            </button>
          </div>
        </div>
      </div>
    );
  };

  const LoginView = () => {
    const [formData, setFormData] = useState({ username: '', password: '' });
    const [localError, setLocalError] = useState('');

    const handleSubmit = async (e) => {
      e.preventDefault();
      try {
        const data = await apiFetch('/auth/sign-in', {
          method: 'POST',
          body: JSON.stringify(formData)
        });

        // 로그인 성공 시 응답받은 accessTokenExpiration 적용 (없을 시 기본 1시간)
        const accessExpDuration = data.accessTokenExpiration * 1000 || 3600000;

        setAuth({ token: data.accessToken, username: formData.username });
        setTokenExpirations({
          access: Date.now() + accessExpDuration,
        });
        localStorage.setItem('accessExp', Date.now() + accessExpDuration)
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('username', formData.username);
        setView('list');

      } catch (err) {
        setLocalError(err.message);
      }
    };

    return (
      <div className="max-w-md mx-auto mt-20 p-8 bg-white rounded-2xl shadow-lg border border-gray-100">
        <h2 className="text-2xl font-bold text-center text-gray-900 mb-8">로그인</h2>
        {localError && <div className="mb-4 p-3 bg-red-50 text-red-600 text-sm rounded-lg border border-red-100">{localError}</div>}
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">아이디</label>
            <input
              type="text" required
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
              value={formData.username} onChange={e => setFormData({...formData, username: e.target.value})}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
            <input
              type="password" required
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
              value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})}
            />
          </div>
          <button type="submit" className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg shadow-sm transition-all">
            로그인
          </button>
        </form>
      </div>
    );
  };

  const SignupView = () => {
    const [formData, setFormData] = useState({ username: '', password: '', name: '' });
    const [localError, setLocalError] = useState('');

    const handleSubmit = async (e) => {
      e.preventDefault();
      try {
        await apiFetch('/auth/sign-up', {
          method: 'POST',
          body: JSON.stringify(formData)
        });
        alert('회원가입이 완료되었습니다. 로그인해주세요.');
        setView('login');
      } catch (err) {
        setLocalError(err.message);
      }
    };

    return (
      <div className="max-w-md mx-auto mt-20 p-8 bg-white rounded-2xl shadow-lg border border-gray-100">
        <h2 className="text-2xl font-bold text-center text-gray-900 mb-8">회원가입</h2>
        {localError && <div className="mb-4 p-3 bg-red-50 text-red-600 text-sm rounded-lg border border-red-100">{localError}</div>}
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">아이디</label>
            <input
              type="text" required maxLength="50"
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
              value={formData.username} onChange={e => setFormData({...formData, username: e.target.value})}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
            <input
              type="password" required
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
              value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">이름</label>
            <input
              type="text" required maxLength="50"
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
              value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})}
            />
          </div>
          <button type="submit" className="w-full py-3 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg shadow-sm transition-all">
            가입하기
          </button>
        </form>
      </div>
    );
  };

  const ListView = () => {
    // 로컬 검색 폼 상태
    const [localType, setLocalType] = useState(searchParams.type);
    const [localKeyword, setLocalKeyword] = useState(searchParams.keyword);

    // 검색 실행 핸들러
    const handleSearch = (e) => {
      e.preventDefault();
      const newParams = { type: localType, keyword: localKeyword.trim() };
      setSearchParams(newParams);
      fetchContents(newParams);
    };

    // 검색 초기화 핸들러
    const handleResetSearch = () => {
      setLocalType('title');
      setLocalKeyword('');
      setSearchParams({ type: 'title', keyword: '' });
      fetchContents({ type: 'title', keyword: '' });
    };

    return (
      <div className="max-w-5xl mx-auto mt-8 px-4">
        <div className="flex justify-between items-end mb-6">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">전체 컨텐츠</h2>
            <p className="text-sm text-gray-500 mt-1">총 {contents.length}개의 게시물이 있습니다.</p>
          </div>
          {auth.token && (
            <button
              onClick={() => { setSelectedContent(null); setView('form'); }}
              className="flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors shadow-sm"
            >
              <Plus className="w-4 h-4 mr-2" /> 새 글 쓰기
            </button>
          )}
        </div>

        {/* --- 검색 바 UI --- */}
        <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-200 mb-6">
          <form onSubmit={handleSearch} className="flex flex-wrap sm:flex-nowrap gap-3 items-center">
            <select
              value={localType}
              onChange={(e) => setLocalType(e.target.value)}
              className="px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none text-sm bg-white min-w-[100px]"
            >
              <option value="title">제목</option>
              <option value="createdBy">작성자</option>
            </select>

            <div className="relative flex-1 min-w-[200px]">
              <Search className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="검색어를 입력하세요..."
                value={localKeyword}
                onChange={(e) => setLocalKeyword(e.target.value)}
                className="w-full pl-9 pr-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all text-sm"
              />
            </div>

            <button type="submit" className="px-6 py-2.5 bg-gray-800 text-white text-sm font-medium rounded-lg hover:bg-gray-900 transition-colors whitespace-nowrap shadow-sm">
              검색
            </button>

            {searchParams.keyword && (
              <button
                type="button"
                onClick={handleResetSearch}
                className="px-4 py-2.5 bg-gray-100 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-200 transition-colors whitespace-nowrap flex items-center"
              >
                <RotateCcw className="w-4 h-4 mr-1" /> 초기화
              </button>
            )}
          </form>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          {loading ? (
            <div className="p-8 text-center text-gray-500">로딩 중...</div>
          ) : error ? (
            <div className="p-8 text-center text-red-500">{error}</div>
          ) : contents.length === 0 ? (
            <div className="p-16 text-center text-gray-500 flex flex-col items-center">
              <Search className="w-12 h-12 text-gray-300 mb-4" />
              <p>검색 조건에 맞는 컨텐츠가 없습니다.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse min-w-[600px]">
                <thead>
                <tr className="bg-gray-50 border-b border-gray-200">
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider w-16 text-center">ID</th>
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">제목</th>
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider w-32 text-center">작성자</th>
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider w-24 text-center">조회수</th>
                  <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider w-32 text-center">작성일</th>
                </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                {contents.map((item) => (
                  <tr
                    key={item.id}
                    onClick={() => fetchContentDetail(item.id)}
                    className="hover:bg-blue-50/50 cursor-pointer transition-colors group"
                  >
                    <td className="px-6 py-4 text-sm text-gray-500 text-center">{item.id}</td>
                    <td className="px-6 py-4 text-sm font-medium text-gray-900 group-hover:text-blue-600 transition-colors">{item.title}</td>
                    <td className="px-6 py-4 text-sm text-gray-600 text-center">{item.createdBy}</td>
                    <td className="px-6 py-4 text-sm text-gray-500 text-center">{item.viewCount}</td>
                    <td className="px-6 py-4 text-sm text-gray-500 text-center">
                      {new Date(item.createdDate).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    );
  };

  const DetailView = () => {
    if (!selectedContent) return null;
    const isOwner = auth.username === selectedContent.createdBy;

    const handleDelete = async () => {
      if (!window.confirm('정말 삭제하시겠습니까?')) return;
      try {
        await apiFetch(`/contents/${selectedContent.id}`, { method: 'DELETE' });
        setView('list');
      } catch (err) {
        alert(err.message);
      }
    };

    return (
      <div className="max-w-4xl mx-auto mt-8 px-4">
        <button onClick={() => setView('list')} className="flex items-center text-sm font-medium text-gray-500 hover:text-gray-900 mb-6 transition-colors">
          <ArrowLeft className="w-4 h-4 mr-1" /> 목록으로
        </button>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="p-8 border-b border-gray-100">
            <h1 className="text-3xl font-bold text-gray-900 mb-4">{selectedContent.title}</h1>
            <div className="flex flex-wrap items-center text-sm text-gray-500 gap-6">
              <span className="flex items-center"><User className="w-4 h-4 mr-1.5" /> {selectedContent.createdBy}</span>
              <span className="flex items-center"><Clock className="w-4 h-4 mr-1.5" /> {new Date(selectedContent.createdDate).toLocaleString()}</span>
              <span className="flex items-center"><Eye className="w-4 h-4 mr-1.5" /> 조회수 {selectedContent.viewCount}</span>
            </div>
          </div>

          <div className="p-8 min-h-[200px] text-gray-800 whitespace-pre-wrap leading-relaxed text-lg">
            {selectedContent.description || <span className="text-gray-400 italic">내용이 없습니다.</span>}
          </div>

          {isOwner && (
            <div className="p-4 bg-gray-50 border-t border-gray-100 flex justify-end space-x-3">
              <button
                onClick={() => setView('form')}
                className="flex items-center px-4 py-2 bg-white border border-gray-300 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-50 transition-colors shadow-sm"
              >
                <Edit className="w-4 h-4 mr-2" /> 수정
              </button>
              <button
                onClick={handleDelete}
                className="flex items-center px-4 py-2 bg-red-50 text-red-600 border border-red-200 text-sm font-medium rounded-lg hover:bg-red-100 transition-colors shadow-sm"
              >
                <Trash2 className="w-4 h-4 mr-2" /> 삭제
              </button>
            </div>
          )}
        </div>
      </div>
    );
  };

  const FormView = () => {
    const isEdit = !!selectedContent;
    const [formData, setFormData] = useState({
      title: isEdit ? selectedContent.title : '',
      description: isEdit ? selectedContent.description : ''
    });

    const handleSubmit = async (e) => {
      e.preventDefault();
      try {
        if (isEdit) {
          await apiFetch(`/contents/${selectedContent.id}`, {
            method: 'PATCH',
            body: JSON.stringify(formData)
          });
          fetchContentDetail(selectedContent.id); // 수정 후 상세조회로 이동
        } else {
          await apiFetch('/contents', {
            method: 'POST',
            body: JSON.stringify(formData)
          });
          setView('list');
        }
      } catch (err) {
        alert(err.message);
      }
    };

    return (
      <div className="max-w-4xl mx-auto mt-8 px-4">
        <button onClick={() => setView(isEdit ? 'detail' : 'list')} className="flex items-center text-sm font-medium text-gray-500 hover:text-gray-900 mb-6 transition-colors">
          <ArrowLeft className="w-4 h-4 mr-1" /> 취소
        </button>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">{isEdit ? '컨텐츠 수정' : '새 컨텐츠 작성'}</h2>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">제목 <span className="text-red-500">*</span></label>
              <input
                type="text" required maxLength="100" placeholder="제목을 입력하세요 (최대 100자)"
                className="w-full px-4 py-3 rounded-xl border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all text-lg font-medium"
                value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">내용</label>
              <textarea
                maxLength="255" rows="8" placeholder="내용을 입력하세요 (최대 255자)"
                className="w-full px-4 py-3 rounded-xl border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all resize-none"
                value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})}
              />
            </div>
            <div className="flex justify-end pt-4 border-t border-gray-100">
              <button type="submit" className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl shadow-sm transition-all text-lg w-full sm:w-auto">
                {isEdit ? '수정 완료' : '등록하기'}
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-gray-50 font-sans">
      <Header />
      {/* 로그인 시 나타나는 Token Manager Banner 추가 */}
      <TokenManagerBanner />

      <main className="pb-20">
        {view === 'login' && <LoginView />}
        {view === 'signup' && <SignupView />}
        {view === 'list' && <ListView />}
        {view === 'detail' && <DetailView />}
        {view === 'form' && <FormView />}
      </main>
    </div>
  );
}