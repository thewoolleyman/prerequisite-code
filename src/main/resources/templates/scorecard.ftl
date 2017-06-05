<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width">

    <link rel="apple-touch-icon" sizes="180x180"
          href="/apple-touch-icon.png">
    <link rel="icon" type="image/png" sizes="32x32"
          href="/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="16x16"
          href="/favicon-16x16.png">
    <link rel="manifest" href="/manifest.json">
    <meta name="theme-color" content="#2c3248">

    <title>TITLE HERE</title>
    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css?family=Merriweather:300,400|Open+Sans:300,600">
    <link rel="stylesheet" href="/app.css">
</head>

<body>

<header>
    <a href="/"><img src="/images/caddy-logo.svg" alt="PAL CADDY"></a>
</header>

<div class="main-content">
    <div>
        <section>
            <h2>SCORECARD</h2>
            <h1>${studentName}</h1>
            <div class="desktop-only grid">
                <div class="grid-content">
                    <table>
                        <thead>
                        <tr>
                        <#list units as unit>
                            <th class="title">
                                <div>${unit.name}</div>
                            </th>
                            <#list unit.assignments as assignment>
                                <th>
                                    <div>${assignment.name}</div>
                                </th>
                            </#list>
                        </#list>
                        </tr>
                        <tr>
                        <#list units as unit>
                            <th></th>
                            <#list unit.assignments as assignment>
                                <th></th>
                            </#list>
                        </#list>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                        <#list units as unit>
                            <td></td>
                            <#list unit.assignments as assignment>
                                <#assign attempts = assignment.attempts>

                                <#if assignment.success == true>
                                    <#assign class="success">
                                <#elseif assignment.success == false>
                                    <#assign class="failure">
                                </#if>

                                <#if assignment.attempts == 0>
                                    <#assign class="unknown">
                                    <#assign attempts = "-">
                                </#if>

                                <td class="${class}">${attempts}</td>
                            </#list>
                        </#list>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div></div>
            <div></div>
        </section>
    </div>
</div>
</body>
</html>
