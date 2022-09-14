class LocalStorage {
    TOKEN_KEY;

    constructor() {
        this.TOKEN_KEY = 'token';
    }

    setToken(token) {
        localStorage.setItem(this.TOKEN_KEY, token);
    }

    getToken() {
        const token = localStorage.getItem(this.TOKEN_KEY);
        if (token !== '') {
            return token;
        }
        return '';
    }

    removeToken() {
        localStorage.removeItem(this.TOKEN_KEY);
    }

    isTokenValid = () => {
        const token = this.getToken();
        if (token === null)
            return false;
        if (token === '') {
            this.removeToken();
            return false;
        }

        try {
            const t = JSON.parse(atob(token.split('.')[1]));
            const expirationDate = t.exp;
            const now = Math.floor(Date.now() / 1000);
            if (expirationDate - now > 0) {
                return true;
            }
            this.removeToken();
            return false;
        } catch(e) {
            this.removeToken();
            return false;
        }
    }
}

export default LocalStorage;