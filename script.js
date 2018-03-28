window.ReMed = new function(){
	this.print = window.java.print;

	this.authUser = function(username, password){
		if(arguments.length==0)
			return window.java.action2();
		else
			return window.java.action3(username, password);
	}

	this.getUserDetailsJSON = window.java.action4;
}

function palleteHide(ele){
	ele.parentElement.classList.remove("pallete-enlarge");
	setTimeout(function(){
		ele.parentElement.classList.add("pallete-invisible")
	},400);
}

var dataObj;
var z=10;

function $(ele){ return document.getElementById(ele); }
function $$(ele){ return document.getElementsByClassName(ele); }
function $q(ele){ return document.querySelector(ele); }
function $$q(ele){ return document.querySelectorAll(ele); }

function authLogin(){
	var email = $("emailx").value;
	var password = $("passwordx").value;
	if(window.ReMed.authUser(email, password)){
		$$("login-dialog")[0].classList.remove("focus");
		dataObj = window.ReMed.getUserDetailsJSON({});
		dataObj["today"] = dataObj["medicines"];
		//alert(dataObj.medicines.length);
		initApp();
	}
}

function signUp(){
	var name = $('name').value;
	var age = parseInt($('age').value);
	var email = $('email').value;
	var password = $('password').value;

	var id = window.java.print5(name, age, email, password);
	dataObj = {"medicines":[], "missedMedicines":[], "user_id":id};

	//alert(id);
	initApp();
}

window.addEventListener('load', function(){
	if(ReMed.authUser()){
		dataObj = window.ReMed.getUserDetailsJSON({});
		dataObj["today"] = dataObj["medicines"];
		initApp();
	}else{
		$q(".signup-dialog").classList.add("focus");
	}
});

function logout(){
	window.java.print4();
}

function initApp(){
	document.body.style.backgroundColor="white";
	document.getElementById('date').innerHTML = (new Date()).getDate();
	$$("signup-dialog")[0].style.display="none";
	$$("login-dialog")[0].style.display="none";
	window.ReMed.print("App Initialised!");
	$q("header").classList.add("focus");
	$q(".header-seperator").classList.add("header-seperator-expand");

	setTimeout(function(){
		palletesShow(".modal-today", ".today-title", ".today-pallete");
	},800);

	initModal(".modal-today", "today-pallete", "today");
	initModal(".modal-medicines", "medicine-pallete", "medicines");
	initModal(".modal-missed", "missed-pallete", "missedMedicines");

}

function initModal(modalClass, modalPalleteClass, dataMappingProperty){
	var palleteContainerNode = $q(modalClass).querySelector(".palletes-container");
	createPalleteNodesUsingDataMap(palleteContainerNode, modalPalleteClass, dataMappingProperty);
}

function createPalleteNodesUsingDataMap(targetNode, palleteClass, property){
	var data = dataObj[property];
	for(var i=0; i<data.length; i++){
		
		var d = data[i];
		if(d.taken==1 && property=="today"){
			continue;
		}
		var hours;
		var title = property == "medicines" ? d.medicineName : ( property == "missedMedicines" ? d.date : (d.time - (new Date().getHours())) + " Hours" );
		targetNode.appendChild(
			createPalleteNode(
				palleteClass,
				title,
				d.amount + " " + d.type + (d.amount>1?"s":""),
				property=="medicines"?d.frequency : d.medicineName,
				d.time > 12 ? (d.time - 12) + " PM" : d.time + " AM",
				property == "medicines" ? "edit" : ( property == "missedMedicines" ? "Reason" : "Taken" ),
				property == "medicines" ? editMedicine : ( property == "missedMedicines" ? editReason : takenMedicine),
				{"id": d.id}
			)
		);
	}
}

function editMedicine(){
	$q(".medicine-edit").classList.add("medicine-dialog-focus");
	$q(".background-blur").style.zIndex = "9998";
	$q(".background-blur").style.opacity = "1";

	var id = this.getAttribute("data-id");
	$('edit-medicine-save').setAttribute("data-id", id);
	var element = dataObj.medicines.filter(function(x){return x.id==id})[0];
	$('edit-medicine-name').value = element.medicineName;
	$('edit-medicine-count-type').value = element.amount + " "+ element.type;
	$('edit-medicine-frequency').value = element.frequency;
	$('edit-medicine-time').value = element.time;

}

function hideEditMedicineDialog(){
	var id = parseInt($('edit-medicine-save').getAttribute("data-id"));
	var medicineName = $('edit-medicine-name').value;
	var amount = parseInt(($('edit-medicine-count-type').value).split(" ")[0]);
	var type = ($('edit-medicine-count-type').value).split(" ")[1];
	var frequency = $('edit-medicine-frequency').value;
	var time = parseInt($('edit-medicine-time').value);

	//window.java.action5(id, medicineName, amount, type, frequency, time );

	for(i in dataObj.medicines){
		if(dataObj.medicines[i].id == id){
			dataObj.medicines[i].medicineName = medicineName;
			dataObj.medicines[i].amount = amount;
			dataObj.medicines[i].type = type;
			dataObj.medicines[i].frequency = frequency;
			dataObj.medicines[i].time = time;
			break;
		}
	}

	palletesHide(".medicines-title", ".medicine-pallete");
	setTimeout(function(){
		$q(".modal-medicines .palletes-container").innerHTML='<div class="pallete medicine-pallete add-pallete" onclick="addMedicine()"><div class="img-container"><img src="add.svg"></div><div class="text">Add a medicine</div></div>';
		initModal(".modal-medicines", "medicine-pallete", "medicines");
		palletesShow(".modal-medicines", ".medicines-title", ".medicine-pallete");
	},600);

	$q(".medicine-edit").classList.remove("medicine-dialog-focus");
	$q(".background-blur").style.zIndex = "-1";
	$q(".background-blur").style.opacity = "0";

}

function addMedicine(){
	$q(".medicine-add").classList.add("medicine-dialog-focus");
	$q(".background-blur").style.zIndex = "9998";
	$q(".background-blur").style.opacity = "1";

	var id = window.java.print3();
	//alert(id);
	//var id = 100;
	$('add-medicine-save').setAttribute("data-id", id);

	dataObj.medicines.push({"id":id});
}

function hideAddMedicineDialog(){
	var id = parseInt($('add-medicine-save').getAttribute("data-id"));
	var medicineName = $('add-medicine-name').value;
	var amount = parseInt(($('add-medicine-count-type').value).split(" ")[0]);
	var type = ($('add-medicine-count-type').value).split(" ")[1];
	var frequency = $('add-medicine-frequency').value;
	var time = parseInt($('add-medicine-time').value);

	window.java.action5(id, medicineName, amount, type, frequency, time );

	for(i in dataObj.medicines){
		if(dataObj.medicines[i].id == id){
			dataObj.medicines[i]["medicineName"] = medicineName;
			dataObj.medicines[i]["amount"] = amount;
			dataObj.medicines[i]["type"] = type;
			dataObj.medicines[i]["frequency"] = frequency;
			dataObj.medicines[i]["time"] = time;
			break;
		}
	}
	dataObj.today = dataObj.medicines;

	palletesHide(".medicines-title", ".medicine-pallete");
	setTimeout(function(){
		$q(".modal-medicines .palletes-container").innerHTML='<div class="pallete medicine-pallete add-pallete" onclick="addMedicine()"><div class="img-container"><img src="add.svg"></div><div class="text">Add a medicine</div></div>';
		initModal(".modal-medicines", "medicine-pallete", "medicines");
		palletesShow(".modal-medicines", ".medicines-title", ".medicine-pallete");
	},600);

	$q(".modal-today .palletes-container").innerHTML = "";
	initModal(".modal-today", "today-pallete", "today");

	$q(".medicine-add").classList.remove("medicine-dialog-focus");
	$q(".background-blur").style.zIndex = "-1";
	$q(".background-blur").style.opacity = "0";
}

function editReason(){
	$q(".missed-reason-dialog").classList.add("missed-reason-dialog-focus");
	$q(".background-blur").style.zIndex = "9998";
	$q(".background-blur").style.opacity = "1";

	var id = this.getAttribute("data-id");
	$('edit-reason-save').setAttribute("data-id", id);

	var element = dataObj.missedMedicines.filter(function(x){return x.id==id})[0];
	$('reason').value = element.reason;

}
function hideReasonMissedDialog(){
	var reason = $('reason').value;
	var id = parseInt($('edit-reason-save').getAttribute("data-id"));

	window.java.print1(id, reason);

	for(i in dataObj.missedMedicines){
		if(dataObj.missedMedicines[i].id == id){
			dataObj.missedMedicines[i].reason = reason;
			break;
		}
	}

	$q(".missed-reason-dialog").classList.remove("missed-reason-dialog-focus");
	$q(".background-blur").style.zIndex = "-1";
	$q(".background-blur").style.opacity = "0";	
}

function takenMedicine(){
	window.java.print2(parseInt(this.getAttribute("data-id")), 1);
	palleteHide(this);
}

function createPalleteNode(palleteClass, title, text1, text2, text3, buttonText, buttonAction, buttonAttributes){
	var node = document.createElement("div");
	node.className = "pallete " + palleteClass;

	node.appendChild(createSubPalleteNode("pallete-title", title));
	node.appendChild(createSubPalleteNode("pallete-text-1",text1));
	node.appendChild(createSubPalleteNode("pallete-text-2",text2));
	node.appendChild(createSubPalleteNode("pallete-text-3",text3));
	
	subNode = createSubPalleteNode("pallete-button", buttonText);
	subNode.onclick = buttonAction;
	for(key in buttonAttributes){
		if(buttonAttributes.hasOwnProperty(key)){
			subNode.setAttribute("data-"+key, buttonAttributes[key]);
		}
	}
	node.appendChild(subNode);

	return node;
}

function createSubPalleteNode(nodeClass, text){
	var subNode = document.createElement("div");
		subNode.classList.add(nodeClass);
		subNode.innerHTML = text;

	return subNode;
}

function palletesShow(modal, title, palleteClass){
	z++;
	$q(modal).style.zIndex = z;
	$q(title).style.opacity = 1;
	var nodes = $$q(palleteClass);
	var i = 0;
	var interval = setInterval(function(){
		if(i==nodes.length){
			clearInterval(interval);
		}else{
			nodes[i].classList.add("pallete-enlarge");
			i++;
		}
	},40);
}

function palletesHide(title, palleteClass){
	$q(title).style.opacity = 0;
	var nodes = $$q(palleteClass);
	var i = 0;
	var interval = setInterval(function(){
		if(i==nodes.length){
			clearInterval(interval);
		}else{
			nodes[i].classList.remove("pallete-enlarge");
			i++;
		}
	},40);
}

function changeTab(ele){
	var nodes = $$q(".button");
	for(var i=0; i<nodes.length; i++){
		nodes[i].classList.remove("selected");
	}
	ele.classList.add("selected");

	/* Hard Coded - need to change in future */
	switch(ele.getAttribute("data-id")){
		case "1":
			palletesHide(".medicines-title", ".medicine-pallete");
			palletesHide(".missed-title", ".missed-pallete");
			setTimeout(function(){
				palletesShow(".modal-today", ".today-title", ".today-pallete");
			},600);
			break;
		case "2":
			palletesHide(".today-title", ".today-pallete");
			palletesHide(".medicines-title", ".medicine-pallete");
			setTimeout(function(){
				palletesShow(".modal-missed", ".missed-title", ".missed-pallete");
			},600);
			break;
		case "3":
			palletesHide(".missed-title", ".missed-pallete");
			palletesHide(".today-title", ".today-pallete");
			setTimeout(function(){
				palletesShow(".modal-medicines", ".medicines-title", ".medicine-pallete");
			},600);
			break;
	}
}

$("login-focus-trigger").onclick = function(){
	$$("signup-dialog")[0].classList.remove("focus");
	setTimeout(function(){
		$$("login-dialog")[0].classList.add("focus");
	}, $$('dialog')[0].style.transitionDuration);
}

$("signup-focus-trigger").onclick = function(){
	$$("login-dialog")[0].classList.remove("focus");
	setTimeout(function(){
		$$("signup-dialog")[0].classList.add("focus");
	}, $$('dialog')[0].style.transitionDuration);
}
