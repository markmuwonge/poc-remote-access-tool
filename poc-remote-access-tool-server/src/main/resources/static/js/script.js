let serverUrl = "localhost:8080"
let websocket = undefined;
let dataTable = undefined;

setHtml();
setWebSocket();
setEventHandlers();

function setHtml() {
    $("table").attr("id", getDataTableId());
}

function setWebSocket() {
    const webSocketUrl = `ws://${serverUrl}/ws`;
    websocket = new WebSocket(webSocketUrl);
    websocket.onopen = function (e) {
        console.log(`Connected to ${webSocketUrl}`);
        websocket.send("tcp_server_status".toUpperCase())
        websocket.send("tcp_server_connections".toUpperCase())
    };
    websocket.onclose = function (e) {
        console.log(`Disconnected from ${webSocketUrl}`);
        location.reload()
    };
    websocket.onmessage = function (e) {
        console.log(`Received ${e.data} from ${webSocketUrl}`)
        const data = JSON.parse(e.data)
        if (data.type.toLowerCase() === "tcp_server_status") {
            const tcpServerButtonClasses = $("#tcp_server_button").attr("class").split(" ");
            tcpServerButtonClasses.pop()
            tcpServerButtonClasses.push(
                data.value.toLowerCase() === "stopped" ? "btn-danger" : "btn-success"
            )
            $("#tcp_server_button").attr("class", tcpServerButtonClasses.join(" "))
        } else if (data.type.toLowerCase() === "tcp_server_connections") {
            setTCPServerConnectionTable(data.value)
        }
    }
}

function setEventHandlers() {
    $("#tcp_server_button").click(function (event) {
        if (websocket === undefined) return;
        websocket.send(
            $("#tcp_server_button").attr("class").includes("danger") ?
                "START_TCP_SERVER" :
                "STOP_TCP_SERVER"
        )
    });
}

async function setTCPServerConnectionTable(tcpServerConnections) {
    if (dataTable === undefined) {
        const tcpServerConnectionFields = await getTCPServerConnectionFields()
        if (!tcpServerConnectionFields) return;
        dataTable = $("#" + getDataTableId()).DataTable({
            columns: tcpServerConnectionFields.map((tcpServerConnectionField) => {
                return { title: tcpServerConnectionField, data: tcpServerConnectionField }
            }),
            select: {

            }
        });
    }

    const dataTableData = dataTable.data()
    const dataTableRows = Array.from(Array(dataTableData.length).keys())
        .map((index) => dataTableData[index]
        )

    tcpServerConnections.forEach((tcpServerConnection) => {
        if (!_.find(dataTableRows, tcpServerConnection)) {
            dataTable.rows.add([tcpServerConnection]).draw();
        }
    })

    dataTableRows.forEach((dataTableRow, index) => {
        if (!_.find(tcpServerConnections, dataTableRow)) {
            dataTable.row(index).remove().draw();
        }
    })
}

function getTCPServerConnectionFields() {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", `http://${serverUrl}/tcp_server_connection_fields`);
        xhr.send();
        xhr.onload = () => {
            if (xhr.readyState == 4 && xhr.status == 200) {
                resolve(JSON.parse(xhr.response))
            } else {
                reject()
            }
        };
    }).then((value) => {
        return value
    })
        .catch(() => {
            console.log("Unable to get TCP server connection fields")
            return false;
        })
}

function getDataTableId() {
    return "tcp_server_connections";
}