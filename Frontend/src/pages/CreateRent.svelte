<script >
    import ValidationError from "../components/ValidationError.svelte";
    import ConfirmNotification from "../components/ConfirmNotification.svelte";
    import {notify} from "../stores/notifier.svelte.js";
    import {fetchWithJwt, isAdmin, isManager, isClient} from "../stores/JWT.svelte.js";
    import config from "../config";

    let users= $state()
    let workers= $state()
    let selectedUser = $state(null)
    let selectedWorker = $state(null)
    let startDate = $state(null)
    let errors = $state([])
    let confirmNotification

    function getUsers(){
        const uri = `${config.BASE_URL}/users`
        fetchWithJwt(uri, {method: "GET"})
            .then(response => {
            if(response.status === 200){
                response.json().then(result => users = result)
            }
            else {
                notify("Backend error")
            }
        })
    }


    function getWorkers(){
        const uri = `${config.BASE_URL}/workers`
        fetchWithJwt(uri, {method: "GET"}).then(response => {
            if(response.status === 200){
                response.json().then(result => workers = result)
            }
            else {
                notify("Backend error")
            }
        })
    }

    function validate(){
        errors = []
        if(selectedUser === null){
            errors.push("User must be selected")
        }
        if(selectedWorker === null){
            errors.push("Worker must be selected")
        }
        if(startDate === null){
            errors.push("Date must be selected")
        }
        if(new Date(startDate) <= new Date()){
            errors.push("Date must be from the future")
        }
        return(errors.length === 0)
    }

    function createRent() {
        if(!validate()){
            return
        }
        let uri;
        if(isAdmin() || isManager()){
            uri = `${config.BASE_URL}/rents/users/${selectedUser.id}/workers/${selectedWorker.id}`;
        }
        else{
            uri = `${config.BASE_URL}/rents/user/workers/${selectedWorker.id}`;
        }
        fetchWithJwt(uri, {
            method: "POST",
            headers: {'Content-Type': 'application/json;charset=UTF-8'},
            body: JSON.stringify({startDate: startDate})
        }).then(
            response => {
                if (response.ok) {
                    selectedWorker = null;
                    selectedUser = null;
                    startDate = null;
                    notify("Rent created")

                } else if (response.status === 409) {
                    notify("Worker occupied")
                } else if (response.status === 403) {
                    notify("User is inactive")
                }
                else{
                    notify(`Unknown error of status code: ${response.status}`)
                }
            });

    }

    if(isAdmin() || isManager()){
        getUsers();
    }
    else{
        selectedUser = "na pewno nie null"
    }
    getWorkers();
</script>
<div class="w-1/2 h-1/2 top-1/5 left-1/4 absolute bg-white border-red-500 p-20 rounded-2xl">
    <h1 class="text-5xl text-green-600 mb-2">Create rent</h1>
    <form class="p-4 bg-gray-50 border border-gray-300 rounded-lg shadow-md space-y-4">
        {#if isAdmin() || isManager()}
            <label for="user" class="text-green-600 mb-2 text-2xl">User:</label>
            <select name="user" bind:value={selectedUser}
                    class="w-full p-2 mb-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500">
                {#each users as user}
                    <option value="{user}">{user.login}</option>
                {/each}
            </select>
        {/if}

        <label for="worker" class="text-green-600 mb-2 text-2xl">Worker:</label>
        <select name="worker" bind:value={selectedWorker} class="w-full p-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500">
            {#each workers as worker}
                <option value="{worker}">{worker.name}</option>
            {/each}
        </select>

        <label for="date" class="text-green-600 mb-2 text-2xl">Start date:</label>
        <input name="date" type="datetime-local" bind:value={startDate} class="w-full p-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500">
        <input type="button" onclick={confirmNotification.show} value="Create rent" class="px-4 w-full py-2 bg-green-600 text-white font-medium rounded-lg hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500">

    </form>
</div>
    {#each errors as error}
        <ValidationError message={error}/>
    {/each}


<ConfirmNotification bind:this={confirmNotification} message="Are you sure?" accept={()=>{createRent();}}/>