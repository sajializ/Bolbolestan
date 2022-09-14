import './images/logo.png';
import './App.css';
import './css/shared.css';
import './css/index.css';
import Courses from './Courses.js';
import Home from './Home.js';
import Plan from './Plan';
import React from 'react';
import logo from './images/logo.png';
import LocalStorage from "./LocalStorage";

import { Redirect, Route, Switch, Link, BrowserRouter as Router } from "react-router-dom";
import { GuardProvider, GuardedRoute } from 'react-router-guards';

const baseURL = "http://87.247.185.122:32000";

class FooterSection extends React.Component {
  render() {
    return (
        <footer>
          <nav className="navbar navbar-expand-lg text-white">
            <div className="navbar-brand">
              <i className="flaticon-copyright icon-c mr-auto"/>
              <p className="icon-c emphasized-text"> دانشگاه تهران - سامانه جامع بلبل&#8239;ستان</p>
            </div>
            <div className="social-media-links nav-link text-white mr-auto">
              <i className="fab fa-facebook-square"></i>
              <i className="fab fa-linkedin"></i>
              <i className="fab fa-instagram-square"></i>
              <i className="fab fa-twitter-square"></i>
            </div>
          </nav>
        </footer>
    )
  }
}

class Signup extends React.Component {
  constructor(props) {
    super(props);
    this.handleRegister = this.handleRegister.bind(this);
    this.handleFirstname = this.handleFirstname.bind(this);
    this.handleLastname = this.handleLastname.bind(this);
    this.handleStudentID = this.handleStudentID.bind(this);
    this.handleBirthDate = this.handleBirthDate.bind(this);
    this.handleField = this.handleField.bind(this);
    this.handleFaculty = this.handleFaculty.bind(this);
    this.handleLevel = this.handleLevel.bind(this);
    this.handleEmail = this.handleEmail.bind(this);
    this.handlePassword = this.handlePassword.bind(this);
    this.state = {
      firstName: "",
      lastName: "",
      studentId: "",
      birthDate: "",
      field: "",
      faculty: "",
      level: "",
      email : "",
      password:"",
      hasLoggedIn : false
    };
  }

  handleFirstname(event) {
    this.setState(prevState => ({ firstName: event.target.value }));
  }

  handleLastname(event) {
    this.setState(prevState => ({ lastName: event.target.value }));
  }

  handleStudentID(event) {
    this.setState(prevState => ({ studentId: event.target.value }));
  }

  handleBirthDate(event) {
    this.setState(prevState => ({ birthDate: event.target.value }));
  }

  handleField(event) {
    this.setState(prevState => ({ field: event.target.value }));
  }

  handleFaculty(event) {
    this.setState(prevState => ({ faculty: event.target.value }));
  }

  handleLevel(event) {
    this.setState(prevState => ({ level: event.target.value }));
  }

  handleEmail(event) {
    this.setState(prevState => ({email: event.target.value}));
  }

  handlePassword(event) {
    this.setState(prevState => ({password: event.target.value}));
  }

  handleRegister(event) {
    event.preventDefault();
    const requestBody = {
      "id": this.state.studentId,
      "name": this.state.firstName,
      "secondName": this.state.lastName,
      "birthDate": this.state.birthDate,
      "email": this.state.email,
      "password": this.state.password,
      "field": this.state.field,
      "faculty": this.state.faculty,
      "level": this.state.level,
      "status": "مشغول به تحصیل",
      "img": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS4BIMuEBA_uOkHaizmAD3Jo7Xp66uomfXhlw&usqp=CAU"
    };
    const requestOptions = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    };
    fetch(baseURL + '/register', requestOptions)
        .then(response =>
            response.json()
        ).then(data => {
      if (data.status !== 403) {
        this.state.hasLoggedIn = true;
        const ls = new LocalStorage();
        ls.setToken(data.token);
        this.props.history.push("/home");
      }
      else
        alert(data.message);
    });
  }

  render() {
    if (!this.state.hasLoggedIn) {
      return (
          <div>
            <MenuSection/>
            <div className="login-form">
              <form onSubmit={this.handleRegister}>
                <div className="form-group">
                  <input type="text" className="form-control" placeholder="نام" id="name"
                         onChange={this.handleFirstname} required={true} pattern="[\u0600-\u06FF\s]*"/>
                </div>
                <div className="form-group">
                  <input type="text" className="form-control" placeholder="نام خانوادگی" id="lastname"
                         onChange={this.handleLastname} required={true} pattern="[\u0600-\u06FF\s]*"/>
                </div>
                <div className="form-group">
                  <input type="text" className="form-control" pattern={"[0-9]*"} placeholder="شماره دانشجویی"
                         id="student_id" onChange={this.handleStudentID} required={true}/>
                </div>
                <div className="form-group">
                  <input type="date" className="form-control" placeholder="تاریخ تولد" id="birth_date"
                         onChange={this.handleBirthDate} required={true}/>
                </div>
                <div className="form-group">
                  <input type="text" className="form-control" placeholder="رشته" id="field" onChange={this.handleField}
                         required={true} pattern="[\u0600-\u06FF\s]*"/>
                </div>
                <div className="form-group">
                  <input type="text" className="form-control" placeholder="دانشکده" id="faculty"
                         onChange={this.handleFaculty} required={true} pattern="[\u0600-\u06FF\s]*"/>
                </div>
                <div className="form-group">
                  <input type="text" className="form-control" placeholder="مقطع" id="level" onChange={this.handleLevel}
                         required={true} pattern="[\u0600-\u06FF\s]*"/>
                </div>
                <div className="form-group">
                  <input type="email" className="form-control" placeholder="ایمیل" id="email"
                         onChange={this.handleEmail} required={true}/>
                </div>
                <div className="form-group">
                  <input type="password" className="form-control" minLength="8" maxLength="20" placeholder="رمز عبور"
                         id="pwd" onChange={this.handlePassword} required={true}/>
                </div>
                <div className="form-group">
                  <button type="submit" className="btn btn-success">ثبت نام</button>
                </div>
              </form>
            </div>
          </div>

      );
    }
    return (<Redirect to={"/home"}/>);
  }
}

class Login extends React.Component {
  constructor(props) {
    super(props);
    this.handleLogin = this.handleLogin.bind(this);
    this.handleUsername = this.handleUsername.bind(this);
    this.handlePassword = this.handlePassword.bind(this);
    this.state = {username : "", password:"", hasLoggedIn : false};
  }

  handleUsername(event) {
    this.setState(prevState => ({username: event.target.value}));
  }

  handlePassword(event) {
    this.setState(prevState => ({password: event.target.value}));
  }

  handleLogin(event) {
    event.preventDefault();
    const params = {
      "email": this.state.username,
      "password": this.state.password
    };
    const queryString = Object.keys(params).map(function(key) {
      return key + '=' + params[key]
    }).join('&');
    const requestOptions = {
      method: 'POST',
      headers: {
        'content-length': queryString.length,
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
      },
      body: queryString
    };
    fetch(baseURL + '/login', requestOptions)
        .then(response =>
            response.json()
        ).then(data => {
          console.log(data);
      if (data.status !== 403) {
        this.state.hasLoggedIn = true;
        const ls = new LocalStorage();
        ls.setToken(data.token);
        this.props.history.push("/home");
      }
      else
        alert(data.message);
    });
  }

  render() {
      if (!this.state.hasLoggedIn)
        return (
          <div>
            <MenuSection/>
            <div className="login-form">
              <form onSubmit={this.handleLogin}>
                <div className="form-group">
                  <input type="email" className="form-control" placeholder="ایمیل" id="username" onChange={this.handleUsername} required={true}
                  />
                </div>
                <div className="form-group">
                  <input type="password" className="form-control" placeholder="رمز عبور" id="pwd" onChange={this.handlePassword} required={true}/>
                </div>
                <div className="form-group">
                  <button type="submit" className="btn btn-success">ورود</button>
                </div>
              </form>
              <Link to="/restorePassword"> رمز عبور خود را فراموش کرده ام.</Link>
            </div>
          </div>
      );
      return (<Home></Home>);
  }
}

class Email extends React.Component {
  constructor(props) {
    super(props);
    this.handlePasswordRestore = this.handlePasswordRestore.bind(this);
    this.handleEmail = this.handleEmail.bind(this);
    this.state = {email : "", hasLoggedIn : false};
  }

  handleEmail(event) {
    this.setState(prevState => ({email: event.target.value}));
  }

  handlePasswordRestore(event) {
    event.preventDefault();
    const params = {
      "email": this.state.email
    };
    const queryString = Object.keys(params).map(function(key) {
      return key + '=' + params[key]
    }).join('&');
    const requestOptions = {
      method: 'POST',
      headers: {
        'content-length' : queryString.length,
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
      },
      body: queryString
    };
    fetch(baseURL + '/restorePassword', requestOptions)
        .then(response =>
            response.json()
        ).then(data => {
          alert(data.response);
    });
  }

  render() {
    if (!this.state.hasLoggedIn) {
      return (
          <div>
            <MenuSection></MenuSection>
            <div className="login-form">
              <form onSubmit={this.handlePasswordRestore}>
                <div className="form-group">
                  <input type="email" className="form-control" placeholder="ایمیل" id="username" onChange={this.handleEmail} required={true}
                  />
                </div>
                <div className="form-group">
                  <button type="submit" className="btn btn-success">ارسال ایمیل</button>
                </div>
              </form>
            </div>
          </div>
      );
    }
    return (<Home/>);
  }
}

class ChangePassword extends React.Component {
  constructor(props) {
    super(props);
    this.handlePassword = this.handlePassword.bind(this);
    this.handleChangePassword = this.handleChangePassword.bind(this);
    this.state = {password : ""};
  }

  validateToken = (tkn) => {
    const t = JSON.parse(atob(tkn.split('.')[1]));
    const expirationDate = t.exp;
    const now = Math.floor(Date.now() / 1000);
    if (expirationDate - now > 0) {
      return true;
    }
    return false;
  }

  componentDidMount() {
    const tkn = this.props.match.params.token;
    if (!this.validateToken(tkn)) {
      alert("این لینک نامعتبر است");
      this.props.history.push('/login');
    }
  }

  handleChangePassword(event) {
    event.preventDefault();
    const requestBody = {
      "token": this.props.match.params.token,
      "password": this.state.password
    };

    const requestOptions = {
      method: 'POST',
      headers: {
        'content-length' : requestBody.length,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    };

    fetch(baseURL + '/changePassword', requestOptions)
        .then(response =>
            response.json()
        ).then(data => {
      console.log(data);
      if (data.status !== 403) {
        this.props.history.push("/login");
      }
        alert(data.response);
    });
  }

  handlePassword(event) {
    this.setState(prevState => ({password: event.target.value}));
  }

  render() {
    return(
        <div>
          <MenuSection></MenuSection>
        <div className="login-form">
          <form onSubmit={this.handleChangePassword}>
            <div className="form-group">
              <input type="password" minLength={8} className="form-control" placeholder="رمز عبور جدید" id="username" onChange={this.handlePassword} required={true}
              />
            </div>
            <div className="form-group">
              <button type="submit" className="btn btn-success">تغییر</button>
            </div>
          </form>
        </div>
        </div>
    );
  }
}

class MenuSection extends React.Component {
  constructor(props) {
    super(props);
    this.state =
        {
          links:[
            { url : "/login", text : "ورود" },
            { url : "/signup", text : "ثبت نام" }
          ]
        };
  }
  render() {
    return (
        <header>
          <nav className="navbar navbar-expand-lg">
            <img className="logo" src={logo} alt="logo"/>
            {
              this.state.links.map((i, index) => (<Link key= {index} className="nav-link" to={i.url} >{i.text}</Link>))
            }
          </nav>
        </header>
    );
  }
}

const requireLogin = (to, from, next) => {
  const ls = new LocalStorage();
  if (to.meta.auth) {
    if (ls.isTokenValid()) {
      console.log(to);
      console.log(from);
      next();
    } else {
      next.redirect('/login');
    }
  } else {
    if (ls.isTokenValid()) {
      next.redirect('/home');
    }
    else {
      next();
    }
  }
};

function App() {
  return (
    <div className="App">
      <Router>
        <GuardProvider guards={[requireLogin]}>
          <Switch>
            <GuardedRoute path="/home" component={Home} meta={{ auth: true }}/>
            <GuardedRoute path="/login" component={Login} meta={{ auth:false }}/>
            <GuardedRoute path="/signup" component={Signup} meta={{ auth:false }}/>
            <GuardedRoute path="/courses" component={Courses} meta={{ auth: true }}/>
            <GuardedRoute path="/plan" component={Plan} meta={{ auth: true }}/>
            <GuardedRoute path="/restorePassword" component={Email} meta={{ auth:false }}/>
            <GuardedRoute path="/changePassword/:token" component={ChangePassword} />
            <GuardedRoute path="/" component={Login} />
          </Switch>
        </GuardProvider>

      </Router>
      <FooterSection/>
    </div>
  );
}

export default App;
