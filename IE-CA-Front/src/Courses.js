import React from 'react';
import "./css/courses.css";
import logo from './images/logo.png';
import "./css/shared.css";
import LocalStorage from "./LocalStorage";
import { Popover, OverlayTrigger} from "react-bootstrap";
import {Link, Redirect} from "react-router-dom";

const baseURL = "http://87.247.185.122:32000";

function toFarsiNumber(n) {
    const farsiDigits = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];

    return n
        .toString()
        .replace(/\d/g, x => farsiDigits[x]);
}

class MenuSection extends React.Component {
    constructor(props) {
        super(props);
        this.logout = this.logout.bind(this);
        this.state =
            {
                links:[
                    { url : "/home", text : "خانه" },
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
                        this.state.links.map((i, index) => (<Link className="nav-link" to={i.url} >{i.text}</Link>))
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
                            <button className="btn success-bg" onClick={(e)=>(this.logout(e))}>خروج</button>
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

class FilteredCoursesRow extends React.Component {
    constructor(props) {
        super(props);
        this.handleSelect = this.handleSelect.bind(this);
        this.state = {courses: [], selectedCourses: [], selected: false};
    }

    getCapacityClass() {
        if (this.props.offering.registeredStudents < this.props.offering.capacity)
            return "dark-primary-text";
        else return "secondary-text";
    }

    // componentDidMount() {
    //     (document).ready(function(){
    //         ('[data-toggle="popover"]').popover();
    //     });
    // }
    popoverContent() {
        let pres = "";
        this.props.offering.prerequisites.forEach(el => {
            pres += el + "\n"
        });
        if (this.props.offering.prerequisites.length !== 0)
            pres = "<strong>پیش نیازی ها</strong>" + "\n" + pres;
        return pres
            + "<strong>امتحان</strong>"
            + "\n"
            + toFarsiNumber(this.props.offering.examTime.start);
    }

    popover = (
        <Popover id="popover-basic" content="">
            <Popover.Title as="div">{toFarsiNumber(this.props.offering.classTime.time) + "\n" + this.props.offering.classTime.days}</Popover.Title>
            <Popover.Content>
                {this.popoverContent()}
            </Popover.Content>
        </Popover>
    );


    handleSelect() {
        if (this.state.selected)
            this.setState(() => ({ selected: false }));
        else
            this.setState(() => ({ selected: true }));
    }

    render() {
        let type = "warning-badge";
        if (this.props.offering.type === "اصلی")
            type = "success-badge";
        else if (this.props.offering.type === "پایه")
            type = "danger-badge";
        else if (this.props.offering.type === "تخصصی")
            type = "primary-badge";
        let style = "badge " + type +" b-m text-white";
        let upContent = "<div>" + toFarsiNumber(this.props.offering.classTime.time) + "<br />" + this.props.offering.classTime.days + "</div>";

        return (
            <tr style={{height: "10px"}} className={this.state.selected === true && "table-primary"}>
                <td>
                    <button className="btn bg-white p-0" onClick={() => this.props.onAdd(this.props.offering.code, this.props.offering.classCode)}>
                        { this.props.offering.registeredStudents < this.props.offering.capacity
                            ? <i className="fas fa-plus center l-icon rounded info-bg"></i>
                            : <i className="center l-icon rounded secondary-bg far fa-clock"/>
                        }
                    </button>
                </td>
                <td>{toFarsiNumber(this.props.offering.code)}-{toFarsiNumber(this.props.offering.classCode)}</td>
                <td className="dark-primary-text emphasized-text">
                    <span className={"emphasized-text " + this.getCapacityClass()}>{toFarsiNumber(this.props.offering.registeredStudents)}</span>
                    <span className={"emphasized-text " + this.getCapacityClass()}>/</span>
                    <span className={"emphasized-text " + this.getCapacityClass()}>{toFarsiNumber(this.props.offering.capacity)}</span>

                </td>
                <td><span className={style} id = "courseType">{this.props.offering.type}</span></td>
                <td>{this.props.offering.name}</td>
                <td>{this.props.offering.instructor}</td>
                <OverlayTrigger trigger="click" placement="left" overlay={this.popover} defaultShow={false}>
                    <td variant="success" onClick={this.handleSelect}>{toFarsiNumber(this.props.offering.units)}</td>
                </OverlayTrigger>
            </tr>
        );
    }
}

class FilteredCourses extends React.Component {
    constructor(props) {
        super(props);
        this.state = { courses: [] };
    }

    render() {
        return (
            <div className="table-container">
                {
                    this.props.loading === false
                        ?
                        <div>
                            <table className={"filtered-courses"}>
                                <colgroup>
                                    <col span="1" style={{width: "3%", height: "10px"}}/>
                                    <col span="1" style={{width: "10%", height: "10px"}}/>
                                    <col span="1" style={{width: "7%", height: "10px"}}/>
                                    <col span="1" style={{width: "10%", height: "10px"}}/>
                                    <col span="1" style={{width: "20%", height: "10px"}}/>
                                    <col span="1" style={{width: "15%", height: "10px"}}/>
                                    <col span="1" style={{width: "5%", height: "10px"}}/>
                                    <col span="1" style={{width: "15%", height: "10px"}}/>
                                </colgroup>
                                <tr height="10px" className="emphasized-text dark-primary-text dark-border border-left-0 border-right-0">
                                    <th className="s-size"/>
                                    <th className="emphasized-text dark-primary-text">کد</th>
                                    <th className="emphasized-text dark-primary-text">ظرفیت</th>
                                    <th className="m-size emphasized-text dark-primary-text">نوع</th>
                                    <th className="emphasized-text dark-primary-text">نام درس</th>
                                    <th className="emphasized-text dark-primary-text">استاد</th>
                                    <th className="emphasized-text dark-primary-text">واحد</th>
                                    <th className="emphasized-text dark-primary-text" style={{width:"20%"}}>توضیحات</th>
                                </tr>
                                {
                                    this.props.courses.map((c) => (
                                        <FilteredCoursesRow offering={c} onAdd={this.props.onAdd}/>))
                                }
                            </table>
                        </div>
                        : <div className="spinner-border dark-primary-text" role="status"><span
                            className="sr-only">Loading...</span></div>
                }
            </div>
        );
    }
}

class StudentCourses extends React.Component {
    constructor(props) {
        super(props);
        this.state = { courses : this.props.selectedCourses , totalUnits : 0};
    }

    render() {
        return (
            <div className="selected-courses">
                <div className="container rounded dark-border">
                    <div className="item">
                        <h3><span className="badge primary-badge label emphasized-text">دروس انتخاب شده</span></h3>
                    </div>
                    <div className="table-container">
                        {
                            this.props.loading === false
                                ?
                                <table>
                                    <colgroup>
                                        <col span="1" style={{width: "5%"}}/>
                                        <col span="1" style={{width: "20%"}}/>
                                        <col span="1" style={{width: "15%"}}/>
                                        <col span="1" style={{width: "30%"}}/>
                                        <col span="1" style={{width: "15%"}}/>
                                        <col span="1" style={{width: "5%"}}/>
                                    </colgroup>
                                    <tr>
                                        <th className="s-size"/>
                                        <th className="emphasized-text dark-primary-text">وضعیت</th>
                                        <th className="emphasized-text dark-primary-text">کد</th>
                                        <th className="emphasized-text dark-primary-text">نام درس</th>
                                        <th className="emphasized-text dark-primary-text">استاد</th>
                                        <th className="emphasized-text dark-primary-text">واحد</th>
                                    </tr>
                                    {
                                        this.props.selectedCourses.map((i) => (<StudentCourse data={i} onDelete={this.props.onDelete}/>))
                                    }
                                </table>
                                : <div className="spinner-border dark-primary-text" role="status"><span
                                    className="sr-only">Loading...</span></div>
                        }
                    </div>
                    <div className="selected-footer">
                        <div className="float-right emphasized-text dark-primary-text">
                            <b className="emphasized-text dark-primary-text">تعداد واحد ثبت شده: {toFarsiNumber(this.props.totalUnits)}</b>
                        </div>
                        <div className="float-left">
                            <div className="btn icon rounded p-1 secondary-bg " id={"resetButton"}>
                                <button className="btn secondary-bg p-0" onClick={this.props.onReset}>
                                    <i className="fas fa-redo center"/>
                                </button>
                            </div>
                            <button className="btn text-white emphasized-text submit-btn" onClick={this.props.onSubmit}>ثبت نهایی</button>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

class SearchCourses extends React.Component {
    constructor(props) {
        super(props);
        this.state = {searchKey: ""};
    }

    render() {
        return (<div className="search-box secondary-border rounded">
            <form className="form-inline my-2 my-lg-0" onSubmit={this.props.onSubmit}>
                <input className="form-control secondary-border search-input" type="search"
                       placeholder="نام درس" aria-label="Search" onChange={this.props.onChange}/>
                <button className="btn success-bg float-left emphasized-text" type="submit">جستجو
                    <i className="fas fa-search float-left"/></button>
            </form>
        </div>);
    }
}

class StudentCourse extends React.Component {
    constructor(props) {
        super(props);
        this.state = { };
    }

    getStateClass() {
        let status = "finalized";
        if (this.props.data.status === "ثبت نهایی نشده")
            status = "non-finalized";
        if (this.props.data.status === "در انتظار")
            status = "pending";
        if (this.props.data.status === "حذف شده")
            status = "deleted";
        return "status " + status + " rounded bg-white"
    }

    render() {
        return (
            <tr>
                <td>
                    <button className="btn bg-white p-0" onClick={() => this.props.onDelete(this.props.data.code, this.props.data.classCode)}>
                        <i className="l-icon rounded fas fa-trash-alt danger-text"/>
                    </button>
                </td>
                <td>
                    <button className={this.getStateClass()}><b>{this.props.data.status}</b></button>
                </td>
                <td>{toFarsiNumber(this.props.data.code)}-{toFarsiNumber(this.props.data.classCode)}</td>
                <td>{this.props.data.name}</td>
                <td>{this.props.data.instructor}</td>
                <td className="emphasized-text dark-primary-text">{toFarsiNumber(this.props.data.units)}</td>
            </tr>
        )
    }
}

class Courses extends React.Component {
    constructor(props) {
        super(props);
        this.handleSearch = this.handleSearch.bind(this);
        this.handleSearchKey = this.handleSearchKey.bind(this);
        this.handleAdd = this.handleAdd.bind(this);
        this.handleDelete = this.handleDelete.bind(this);
        this.handleReset = this.handleReset.bind(this);
        this.finalize = this.finalize.bind(this);
        this.showAll = this.showAll.bind(this);
        this.showTakhasosi = this.showTakhasosi.bind(this);
        this.showAsli = this.showAsli.bind(this);
        this.showPaye = this.showPaye.bind(this);
        this.showUmumi = this.showUmumi.bind(this);
        this.state = { courses : [], selectedCourses : [], searchKey: "", filteredCourses: [], totalUnits: 0, filter: "All", selectedCourseLoading: false, filteredCourseLoading: false };
    }
    getInfo() {
        this.setState(() => ({ selectedCourseLoading: true }));
        fetch(baseURL + '/students/me/schedule', {
            headers: {
                "authorization": localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                this.setState(() => ({ selectedCourseLoading: false }))
                this.setState(() => ({ selectedCourses : data }))
                fetch(baseURL + '/students/me/totalPassedUnits', {
                    headers: {
                        "authorization": localStorage.getItem('token')
                    }
                })
                    .then(resp => resp.json())
                    .then(data => {
                        this.setState(() => ({ totalUnits : data.totalUnits }))
                    });
            });
    }

    finalize() {
        this.setState(() => ({ selectedCourseLoading: true }));
        fetch(baseURL + '/students/me/schedule/courses/finalize', {
            method: 'POST',
            headers: {
                "authorization": localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                this.setState(() => ({ selectedCourseLoading: false }));
                if (data.errors.length === 0) {
                    this.setState(prevState => ({selectedCourses: data.courses}));
                } else {
                    this.setState(prevState => ({finalizeErrors: data.errors}));
                    this.openModal();
                }
            });
    }

    handleAdd(code, classCode) {
        this.setState(() => ({ selectedCourseLoading: true }));
        fetch(baseURL + "/students/me/schedule/courses/" + code + "/" + classCode, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                'authorization': localStorage.getItem('token')
            },
            body: ""
        }).then(res => res.json())
            .then(data => {
                this.setState(() => ({ selectedCourseLoading: false }));
                if (!data.errors) {
                    const appendedCourses = [...this.state.selectedCourses, data];
                    this.setState(() => ({ selectedCourses: appendedCourses }));
                    fetch(baseURL + '/students/me/totalPassedUnits', {
                        headers: {
                            "authorization": localStorage.getItem('token')
                        }
                    })
                        .then(resp => resp.json())
                        .then(data => {
                            this.setState(() => ({ totalUnits : data.totalUnits }))
                        });
                }
                else {
                    console.log(data.errors);
                    let x = document.getElementById("snackbar");
                    x.className = "show";
                    x.innerText = toFarsiNumber(data.errors[0].message);
                    setTimeout(function(){ x.className = x.className.replace("show", ""); }, 3000);
                }
            });
    }

    handleReset() {
        this.setState(() => ({ selectedCourseLoading: true }));
        fetch(baseURL + "/students/me/schedule/courses/reset", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'authorization': localStorage.getItem('token')
            },
            body: ""
        }).then(res => res.json())
            .then(data => {
                this.setState(() => ({ selectedCourses: data, selectedCourseLoading: false }));
                fetch(baseURL + '/students/me/totalPassedUnits', {
                    headers: {
                        'authorization': localStorage.getItem('token')
                    }
                })
                    .then(resp => resp.json())
                    .then(data => {
                        this.setState(() => ({ totalUnits : data.totalUnits }))
                    });
            });
    }

    showAsli(event) {
        event.preventDefault();
        this.setState(() => ({ filteredCourseLoading: true }));
        fetch(baseURL + '/courses?type=Asli', {
            headers: {
                'authorization': localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                this.setState(() => ({filteredCourses: data, filter: "Asli", filteredCourseLoading: false}));
            });
    }

    showPaye(event) {
        event.preventDefault();
        this.setState(() => ({ filteredCourseLoading: true }));
        fetch(baseURL + '/courses?type=Paaye', {
            headers: {
                'authorization': localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                this.setState(() => ({filteredCourses: data, filter: "Paye", filteredCourseLoading: false }));
            });
    }

    showUmumi(event) {
        event.preventDefault();
        this.setState(() => ({ filteredCourseLoading: true }));
        fetch(baseURL + '/courses?type=Umumi',{
            headers: {
                'authorization': localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                this.setState(() => ({filteredCourses: data, filter: "Umumi", filteredCourseLoading: false }));
            });
    }

    showAll(event) {
        event.preventDefault();
        this.setState(() => ({ filteredCourseLoading: true }));
        fetch(baseURL + '/courses?type=', {
            headers: {
                'authorization': localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                this.setState(() => ({filteredCourses: data, filter: "All", filteredCourseLoading: false }));
            });
    }

    showTakhasosi(event) {
        event.preventDefault();
        this.setState(() => ({ filteredCourseLoading: true }));
        fetch(baseURL + '/courses?type=Takhasosi', {
            headers: {
                'authorization': localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                this.setState(() => ({filteredCourses: data, filter: "Takhasosi", filteredCourseLoading: false }));
            });
    }

    handleSearch(event) {
        event.preventDefault();
        this.setState(() => ({ filteredCourseLoading: true }));
        fetch(baseURL + '/courses/search?keyword=' + this.state.searchKey, {
            headers: {
                'authorization': localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                let filteredData = [];
                if (this.state.filter === "All")
                    filteredData = data
                else {
                    data.forEach(el => {
                        if (this.state.filter === "Takhasosi" && el.type === "تخصصی")
                            filteredData.push(el);
                        if (this.state.filter === "Umumi" && el.type === "عمومی")
                            filteredData.push(el);
                        if (this.state.filter === "Paye" && el.type === "پایه")
                            filteredData.push(el);
                        if (this.state.filter === "Asli" && el.type === "اصلی")
                            filteredData.push(el);
                    });
                }
                this.setState(() => ({ filteredCourses: filteredData, filteredCourseLoading: false }));
            });
    }

    handleDelete(code, classCode) {
        this.setState(() => ({ selectedCourseLoading: true }));
        fetch(baseURL + "/students/me/schedule/courses/" + code + "/" + classCode, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'authorization': localStorage.getItem('token')
            },
            body: ""
        }).then(res => res.json())
            .then(data => {
                this.setState(
                    prevState =>
                        ({
                            selectedCourses: prevState.selectedCourses.filter(c => c.code !== code || c.classCode !== classCode),
                            totalUnits: prevState.totalUnits - data.units,
                            selectedCourseLoading: false
                        })
                );
            });
    }

    handleSearchKey(event) {
        this.setState(() => ({searchKey: event.target.value}));
    }

    render() {
        return (
            <div className="courses-container">
                <MenuSection/>
                <div style={{ marginTop: "100px"}}>
                    <StudentCourses selectedCourses={this.state.selectedCourses} totalUnits={this.state.totalUnits} onDelete={this.handleDelete} onReset={this.handleReset} onSubmit={this.finalize} loading={this.state.selectedCourseLoading}/>
                    <SearchCourses onSubmit={this.handleSearch} onChange={this.handleSearchKey}/>
                    <div>
                        <div className="courses">
                            <div className="container rounded dark-border">
                                <div className="item">
                                    <h3><span
                                        className="badge primary-badge label text-white emphasized-text">دروس ارا‌ئه شده</span>
                                    </h3>
                                </div>
                                <div className="filters emphasized-text">
                                    <button className={this.state.filter === "All" ?
                                        "btn text-secondary active" : "btn text-secondary border-secondary bg-white"} onClick={this.showAll}>همه</button>
                                    <button className={this.state.filter === "Takhasosi" ?
                                        "btn text-secondary active" : "btn text-secondary border-secondary bg-white"} onClick={this.showTakhasosi}>اختصاصی</button>
                                    <button className={this.state.filter === "Asli" ?
                                        "btn text-secondary active" : "btn text-secondary border-secondary bg-white"} onClick={this.showAsli}>اصلی</button>
                                    <button className={this.state.filter === "Paye" ?
                                        "btn text-secondary active" : "btn text-secondary border-secondary bg-white"} onClick={this.showPaye}>پایه</button>
                                    <button className={this.state.filter === "Umumi" ?
                                        "btn text-secondary active" : "btn text-secondary border-secondary bg-white"} onClick={this.showUmumi}>عمومی</button>
                                </div>
                                <FilteredCourses courses={this.state.filteredCourses} onAdd={this.handleAdd} loading={this.state.filteredCourseLoading}/>
                            </div>
                        </div>
                    </div>

                    <div id="snackbar"></div>

                    <div id="errorModal" className="modal">
                        <div className="modal-content">
                            <div className="modal-header">
                                <div className="">
                                    <h5>ثبت با مشکل مواجه شد</h5>
                                    <br />
                                    <div id = "errorMessage"></div>
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button className="btn success-badge secondary-border secondary-text" onClick={this.closeModal}>بازگشت</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    openModal() {
        const modal = document.getElementById("errorModal");
        modal.style.display = "block";
        document.getElementById("errorMessage").innerHTML = "";
        this.state.finalizeErrors.forEach(error => {
            let element = document.createElement("p");
            element.appendChild(document.createTextNode(toFarsiNumber(error.message)));
            element.style.cssText = 'text-align: right';
            document.getElementById('errorMessage').appendChild(element);
        });
    }


    closeModal() {
        const modal = document.getElementById("errorModal");
        modal.style.display = "none";
    }

    componentDidMount() {
        this.setState(() => ({ filteredCourseLoading: true }));
        fetch(baseURL + '/courses', {
            headers: {
                'authorization': localStorage.getItem('token')
            }
        })
            .then(resp => resp.json())
            .then(data => {
                this.setState(() => ({
                    courses : data,
                    filteredCourses: data,
                    filteredCourseLoading: false
                }));
                this.getInfo();
            });
    }
}

export default Courses;