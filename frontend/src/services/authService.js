const KEYCLOAK_URL = 'http://localhost:8180';
const REALM = 'eticaret';
const CLIENT_ID = 'eticaret-frontend';

export const authService = {
  login: async (username, password) => {
    const response = await fetch(
      `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          grant_type: 'password',
          client_id: CLIENT_ID,
          username,
          password,
        }),
      }
    );

    if (!response.ok) throw new Error('Giriş başarısız');

    const data = await response.json();

    // HttpOnly cookie backend tarafından set edilmeli
    // Frontend sadece sessionStorage'da kısa süreli tutar
    sessionStorage.setItem('token', data.access_token);
    return data;
  },

  logout: () => {
    sessionStorage.removeItem('token');
    window.location.href = '/';
  },

  isLoggedIn: () => !!sessionStorage.getItem('token'),
  getToken: () => sessionStorage.getItem('token'),
};