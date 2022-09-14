import React from 'react';
import "./css/schedule.css";
import ReactDOM from 'react-dom';
import logo from './images/logo.png';
import LocalStorage from "./LocalStorage";
import {Link, Redirect} from "react-router-dom";

function toFarsiNumber(n) {
    const farsiDigits = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];

    return n
        .toString()
        .replace(/\d/g, x => farsiDigits[x]);
}

function truncate(n, elementId) {
    let w = document.getElementById(elementId).clientWidth;
    let result = n;
    if (w > 180 && n.length > 19) {
        result = n.substr(0, 17) + "...";
    } else {
        if (w > 110 && n.length > 13 && w < 180) {
            result = n.substr(0, 11) + "...";
        } else {
            if (w > 50 && n.length > 10 && w < 110) {
                result = n.substr(0, 8) + "...";
            }
        }
    }
    return result;
}

class MenuSection extends React.Component {
    constructor(props) {
        super(props);
        this.logout = this.logout.bind(this);
        this.state =
            {
                links:[
                    { url : "/home", text : "خانه" },
                    { url : "/courses", text : "انتخاب واحد" }
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


class Plan extends React.Component {
    constructor(props) {
        super(props);
        this.getData = this.getData.bind(this);
        this.getTerm = this.getTerm.bind(this);
        this.fillPlanData = this.fillPlanData.bind(this);
        this.state = {courses: [], term: 1, loading: false};
    }

    componentDidMount() {
        this.getTerm();
        this.getData();
    }

    getTerm() {
        fetch('http://localhost:8080/students/me/term', {
            headers: {
                "authorization": localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                this.setState(() => ({ term : data.term }));
            });
    }

    getData() {
        this.setState(() => ({ loading : true }));
        fetch('http://localhost:8080/students/me/plan', {
            headers: {
                "authorization": localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                this.setState(() => ({ courses : data, loading: false }));
                this.fillPlanData();
            });
    }

    fillPlanData() {
        this.state.courses.forEach(course => {
            course.days.forEach(day => {
                let classTime = course.start.split(":");
                let style = "center schedule-item ";
                if (course.type === "اصلی")
                    style = style + "asli";
                if (course.type === "عمومی")
                    style = style + "omumi";
                if (course.type === "تخصصی")
                    style = style + "ekhtesasi";
                if (course.type === "پایه")
                    style = style + "paye";
                style = style + " st-" + classTime[0] + "-" + classTime[1];
                if (course.length === 90)
                    style = style + " len-1-5";
                if (course.length === 120)
                    style = style + " len-2";
                if (course.length === 180)
                    style = style + " len-3";
                ReactDOM.render(<div className={style}>
                    <div className="schedule-text">
                        {toFarsiNumber(course.start)}-{toFarsiNumber(course.end)}
                        <br/>
                        <b className="emphasized-text">{truncate(course.name, day + classTime[0])}</b>
                        <br/>
                        <b className="emphasized-text">{course.type}</b>
                    </div>
                </div>, document.getElementById(day + classTime[0]));
            });
        });
    }

    render() {
        return (
            <div class="courses-container">
                <MenuSection/>
                <div style={{ marginTop: "100px" }}>
                    <div className="dark-border bg-white weekly-schedule">

                        {
                            this.state.loading === false ?
                                <div>
                                    <div className="table-label m-3">
                                        <div className="float-right">
                                            <div>
                                                <i className="far fa-calendar-alt float-right dark-text"></i>
                                                &nbsp;
                                                برنامه هفتگی
                                            </div>
                                        </div>
                                        <div className="float-left">ترم {toFarsiNumber(this.state.term)}</div>
                                    </div>
                                    <div className="table-container">
                                        <table class="weekly-plan">
                                            <tr>
                                                <th className="border-right-0"></th>
                                                <th className="border-right-0 day-len">شنبه</th>
                                                <th className="border-right-0 day-len">یک شنبه</th>
                                                <th className="border-right-0 day-len">دوشنبه</th>
                                                <th className="border-right-0 day-len">سه شنبه</th>
                                                <th className="border-right-0 day-len">چهارشنبه</th>
                                                <th className="border-right-0 border-left-0 day-len">پنج شنبه</th>
                                            </tr>
                                            <tr>
                                                <td>۷:۰۰-۸:۰۰</td>
                                                <td id="Saturday7"></td>
                                                <td id="Sunday7"></td>
                                                <td id="Monday7"></td>
                                                <td id="Tuesday7"></td>
                                                <td id="Wednesday7"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                            <tr>
                                                <td>۸:۰۰-۹:۰۰</td>
                                                <td id="Saturday8"></td>
                                                <td id="Sunday8"></td>
                                                <td id="Monday8"></td>
                                                <td id="Tuesday8"></td>
                                                <td id="Wednesday8"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                            <tr>
                                                <td>۹:۰۰-۱۰:۰۰</td>
                                                <td id="Saturday9"></td>
                                                <td id="Sunday9"></td>
                                                <td id="Monday9"></td>
                                                <td id="Tuesday9"></td>
                                                <td id="Wednesday9"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                            <tr>
                                                <td>۱۰:۰۰-۱۱:۰۰</td>
                                                <td id="Saturday10"></td>
                                                <td id="Sunday10"></td>
                                                <td id="Monday10"></td>
                                                <td id="Tuesday10"></td>
                                                <td id="Wednesday10"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                            <tr>
                                                <td>۱۱:۰۰-۱۲:۰۰</td>
                                                <td id="Saturday11"></td>
                                                <td id="Sunday11"></td>
                                                <td id="Monday11"></td>
                                                <td id="Tuesday11"></td>
                                                <td id="Wednesday11"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                            <tr>
                                                <td>۱۲:۰۰-۱۳:۰۰</td>
                                                <td id="Saturday12"></td>
                                                <td id="Sunday12"></td>
                                                <td id="Monday12"></td>
                                                <td id="Tuesday12"></td>
                                                <td id="Wednesday12"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                            <tr>
                                                <td>۱۳:۰۰-۱۴:۰۰</td>
                                                <td id="Saturday13"></td>
                                                <td id="Sunday13"></td>
                                                <td id="Monday13"></td>
                                                <td id="Tuesday13"></td>
                                                <td id="Wednesday13"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                            <tr>
                                                <td>۱۴:۰۰-۱۵:۰۰</td>
                                                <td id="Saturday14"></td>
                                                <td id="Sunday14"></td>
                                                <td id="Monday14"></td>
                                                <td id="Tuesday14"></td>
                                                <td id="Wednesday14"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                            <tr>
                                                <td>۱۵:۰۰-۱۶:۰۰</td>
                                                <td id="Saturday15"></td>
                                                <td id="Sunday15"></td>
                                                <td id="Monday15"></td>
                                                <td id="Tuesday15"></td>
                                                <td id="Wednesday15"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                            <tr>
                                                <td>۱۶:۰۰-۱۷:۰۰</td>
                                                <td id="Saturday16"></td>
                                                <td id="Sunday16"></td>
                                                <td id="Monday16"></td>
                                                <td id="Tuesday16"></td>
                                                <td id="Wednesday16"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                            <tr>
                                                <td>۱۷:۰۰-۱۸:۰۰</td>
                                                <td id="Saturday17"></td>
                                                <td id="Sunday17"></td>
                                                <td id="Monday17"></td>
                                                <td id="Tuesday17"></td>
                                                <td id="Wednesday17"></td>
                                                <td className="border-left-0"></td>
                                            </tr>
                                        </table>
                                    </div>
                                </div>

                                : <div className="spinner-border dark-primary-text" role="status" style={{
                                    margin: "20px",
                                    width: "4rem",
                                    height: "4rem"
                                }}><span
                                    className="sr-only">Loading...</span></div>
                        }
                    </div>
                </div>
            </div>

        );
    }
}

export default Plan;