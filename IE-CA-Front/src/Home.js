import React from 'react';
import "./css/home.css";
import "./css/shared.css";
import logo from './images/logo.png';
import LocalStorage from "./LocalStorage";
import { Link, Redirect} from "react-router-dom";
import { withRouter } from 'react-router-dom';

const baseURL = "http://87.247.185.122:32000";

function toFarsiNumber(n) {
    const farsiDigits = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];

    return n
        .toString()
        .replace(/\d/g, x => farsiDigits[x]);
}

class Home extends React.Component {

    isNotLoggedIn() {
        const ls = new LocalStorage();
        return (ls.getToken() === '');
    }

    render() {
        return (
            <div>
                <MenuSection/>
                <div className="slide-show">
                </div>
                <div className="main light-secondary-bg">
                    <StudentProfile/>
                    <Reports/>
                </div>
            </div>
        );
    }
}

class StudentProfile extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            studentId : "",
            name: "",
            birthDate: "",
            GPA: 0,
            totalPassedUnits:0,
            faculty: "",
            field: "",
            level: "",
            status: "",
            imageUrl: "",
            profileLoading: false
        };
    }

    render() {
        return (
            <div className="profile bg-white">
                { (this.state.profileLoading === false) ?
                    <div>
                        <div className="avatar border border-primary rounded-circle">
                            <img src={this.state.imageUrl} alt="" className=""/>
                        </div>
                        <p><span className="dark-text">نام: </span>{this.state.name}</p>
                        <p><span className="dark-text">شماره دانشجویی: </span>{toFarsiNumber(this.state.studentId)}</p>
                        <p><span className="dark-text">تاریخ تولد: </span>{toFarsiNumber(this.state.birthDate)}</p>
                        <p><span className="dark-text">معدل کل: </span>{toFarsiNumber(this.state.GPA)}</p>
                        <p><span className="dark-text">واحد گذرانده: </span>{toFarsiNumber(this.state.totalPassedUnits)}
                        </p>
                        <p><span className="dark-text">دانشکده: </span>{this.state.faculty}</p>
                        <p><span className="dark-text">رشته: </span>{this.state.field}</p>
                        <p><span className="dark-text">مقطع: </span>{this.state.level}</p>
                        <p className="rounded dark-text dark-border study-state">{this.state.status}</p>
                    </div>
                    : <div className="spinner-border dark-primary-text" role="status" style={{marginTop:"100px", width: "4rem", height: "4rem"}}><span
                        className="sr-only">Loading...</span></div>
                }
            </div>
        );
    }

    componentDidMount() {
        this.getProfileData();
    }

    getProfileData() {
        this.setState(() => ({ profileLoading: true }));
        fetch(baseURL + '/students/me/profile', {
            headers: {
                "Content-Type": "application/xml",
                "authorization": localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                if (data.status === 403) {
                    this.props.history.push("/login");
                }
                this.setState(() => ({
                    studentId: data.studentId,
                    name: data.firstName + " " + data.lastName,
                    birthDate: data.birthDate,
                    GPA: data.GPA.toFixed(2),
                    totalPassedUnits: data.totalPassedUnits,
                    faculty: data.faculty,
                    field: data.field,
                    level: data.level,
                    status: data.status,
                    imageUrl: data.imageUrl,
                    profileLoading: false
                }));
            });

    }
}


class Reports extends React.Component {
    constructor(props) {
        super(props);
        this.state = {reports: [], reportsLoading: false };
    }

    getReportData() {
        this.setState(() => ({ reportsLoading: true }));
        fetch(baseURL + '/students/me/reports', {
            headers: {
                "Origin": "http://localhost:3000/",
                "authorization": localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                if (data.status === 403) {
                    this.props.history.push("/login");
                }
                const tempReports = [];
                Object.keys(data).forEach(function(key) {
                    let value = {};
                    value.term = key;
                    value.report = data[key];
                    tempReports.push(value);
                });
                this.setState(prevState => ({
                    reports: tempReports,
                    reportsLoading: false
                }));
            });

    }

    componentDidMount() {
        this.getReportData();
    }

    render() {
        return (
            <div className="report-body">
                {
                    this.state.reportsLoading === false
                        ? this.state.reports.map((i) => (<ReportCard data = {i}/>))
                        : <div className="spinner-border dark-primary-text" role="status" style={{marginTop:"100px", width: "6rem", height: "6rem"}}>
                            <span className="sr-only">Loading...</span>
                        </div>
                }
            </div>
        );
    }
}


class ReportCardCourseRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};
    }
    getStateClass() {
        let classes = "";
        classes += (this.props.course.state === "قبول" )
            ? "info-border info-text course-passed"
            : "danger-border danger-text course-failed";
        return classes;
    }

    getGradeClass() {
        let classes = "";
        classes += (this.props.course.state === "قبول" )
            ? "info-text"
            : "danger-text";
        return classes;
    }

    render() {
        return (
            <div className="row rounded primary-border course-row">
                <div className="col-1">
                    <b className="dark-text emphasized-text">{toFarsiNumber(this.props.index)}</b>
                </div>
                <div className="col-3">
                    {toFarsiNumber(this.props.course.code)}
                </div>
                <div className="col-3">
                    {this.props.course.name}
                </div>
                <div className="col-2">
                    {toFarsiNumber(this.props.course.units)}&nbsp;واحد
                </div>
                <div className="col-2">
                    <p className={"rounded course " + this.getStateClass()}>{this.props.course.state}</p>
                </div>
                <div className="col-1 border-0">
                    <div className={"emphasized-text border-0 " + this.getGradeClass()}>{toFarsiNumber(this.props.course.grade)}</div>
                </div>
            </div>
        );
    }
}

class ReportCard extends React.Component {
    constructor(props) {
        super(props);
        this.state = { term: 0, GPA: 0 };
    }
    calculateGPA() {
        let sumOfUnits = 0;
        let sumOfGrades = 0;
        this.props.data.report.forEach(el => {
            sumOfGrades += el.units * el.grade;
            sumOfUnits += el.units;
        });
        if (sumOfUnits !== 0)
            this.setState(prevState => ({ GPA : (sumOfGrades / sumOfUnits).toFixed(2) }));
    }

    render() {
        return (
            <div className="report-card">
                <div className="container rounded dark-border bg-white">
                    <div className="item">
                        <h3><span className="badge primary-badge label">کارنامه - ترم {toFarsiNumber(this.props.data.term)}</span></h3>
                    </div>
                    <div className="border-0">
                        {
                            this.props.data.report.map((i, index) => (<ReportCardCourseRow key = {index} course = {i} index={index + 1}/>))
                        }
                    </div>

                    <div className="row average-row">
                        <div className="col align-self-start"></div>
                        <div className="col align-self-center"></div>
                        <div className="col-4 align-self-end">
                            <div className="rounded dark-text dark-border float-left average-btn">معدل: {toFarsiNumber(this.state.GPA)}</div>
                        </div>
                    </div>
                </div>
            </div>);
    }
    componentDidMount() {
        this.calculateGPA();
    }
}


class MenuSection extends React.Component {
    constructor(props) {
        super(props);
        this.logout = this.logout.bind(this);
        this.state =
            {
                links:[
                    { url : "/courses", text : "انتخاب واحد" },
                    { url : "/plan", text : "برنامه هفتگی" }
                ],
                redirectToLogin: false
            };
    }

    openModal() {
        const modal = document.getElementById("myModal");
        modal.style.display = "block";
    }

    closeModal() {
        const modal = document.getElementById("myModal");
        modal.style.display = "none";
    }

    logout(event) {
        event.preventDefault();
        const modal = document.getElementById("myModal");
        modal.style.display = "none";
        const ls = new LocalStorage();
        ls.removeToken();
        this.setState(() => ({ redirectToLogin: true }));
    }

    render() {
        return (
                <header>
                    <nav className="navbar navbar-expand-lg">
                        <img className="logo" src={logo} alt="logo"/>
                        {
                            this.state.links.map((i, index) => (<Link key= {index} className="nav-link" to={i.url} >{i.text}</Link>))
                        }
                        <div className="logout">
                            <button className="nav-link danger-text text-with-icon btn bg-white" onClick={this.openModal}>
                                خروج&nbsp;
                                <div className="float-left">
                                    <i className="fas fa-sign-out-alt center"/>
                                </div>
                            </button>
                        </div>
                    </nav>
                    <div id="myModal" className="modal">
                        <div className="modal-content">
                            <div className="modal-header">
                                <div className="">
                                    <h5>آیا می خواهید از حساب کاربری خود خارج شوید؟</h5>
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button className="btn bg-white secondary-border secondary-text" onClick={this.closeModal}>انصراف</button>
                                <button className="btn success-bg" onClick={(event => this.logout(event))}>خروج</button>
                            </div>
                        </div>
                    </div>
                    {this.state.redirectToLogin === true &&
                        (<Redirect to="/login" />)
                    }
                </header>
        );
    }
}


export default withRouter(Home);