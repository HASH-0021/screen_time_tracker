interface AppUsageProps {
  packageName: string;
  totalTimeInForeground: number;
  appName: string;
  appIconBase64: string;
}

const convertTimeFormat = (totalMilSec: number) => {
  const milliSeconds: number = totalMilSec % 1000;
  const totalSec: number = Math.floor(totalMilSec / 1000);
  const seconds: number = totalSec % 60;
  const totalMin: number = Math.floor(totalSec / 60);
  const minutes: number = totalMin % 60;
  const hours: number = Math.floor(totalMin / 60);
  if (hours) {
    return `${hours}h ${minutes}m`;
  } else if (minutes) {
    return `${minutes}m ${seconds}s`;
  } else if (seconds) {
    return `${seconds}s`;
  } else {
    return `${milliSeconds}ms`;
  }
};

const getTotalScreenTime = (usageStats: AppUsageProps[]) => {
  let totalScreenTime = 0;
  for (const appUsage of usageStats) {
    totalScreenTime += appUsage.totalTimeInForeground;
  }
  return totalScreenTime;
};

const convertToTimeStamp = (time: number) => {
  const d = new Date(time);
  const months = [
    'JAN',
    'FEB',
    'MAR',
    'APR',
    'MAY',
    'JUN',
    'JUL',
    'AUG',
    'SEP',
    'OCT',
    'NOV',
    'DEC',
  ];
  const date = d.getDate();
  const month = d.getMonth();
  const year = d.getFullYear();
  const hour = d.getHours();
  const hourString = hour < 10 ? `0${hour}` : String(hour);
  const minute = d.getMinutes();
  const minuteString = minute < 10 ? `0${minute}` : String(minute);
  const second = d.getSeconds();
  const secondString = second < 10 ? `0${second}` : String(second);
  return `${date} ${months[month]} ${year}, ${hourString}:${minuteString}:${secondString}`;
};

const decodeEncodedURI = (uriString: string) => {
  const uriComponent: string = uriString.split(
    'content://com.android.externalstorage.documents/',
  )[1];
  return decodeURIComponent(uriComponent);
};

const calculateTotalTime = (data: any) => {
  let total = 0;
  for (const app in data) {
    total += data[app];
  }
  return total;
};

const getWeeklyData = (
  screenTimeData: any,
  year: number,
  month: number,
  date: number,
) => {
  const d = new Date(`${year}-${month}-${date}`);
  const dayNo = d.getDay();
  d.setDate(date - dayNo);
  let newDate = d.getDate(),
    newMonth = d.getMonth(),
    newYear = d.getFullYear();
  let yearlyData: any = {},
    monthlyData: any = {},
    dayData: any = {};
  const weeklyData: any = {};

  for (let index = 0; index < 7; index++) {
    if (index !== 0) {
      d.setDate(newDate + 1);
      (newDate = d.getDate()),
        (newMonth = d.getMonth()),
        (newYear = d.getFullYear());
    }
    if (newYear === year) {
      yearlyData = {...screenTimeData[String(newYear)]};
      monthlyData = {...yearlyData[String(newMonth + 1)]};
      dayData = {...monthlyData[String(newDate)]};
      weeklyData[String(index)] = dayData;
    } else {
      weeklyData[String(index)] = {};
    }
  }
  console.log(screenTimeData);
  return {
    aggregatedWeeklyData: getAggregatedData(weeklyData),
    weeklyData,
  };
};

const getMonthlyData = (screenTimeData: any, year: number, month: number) => {
  const yearlyData = {...screenTimeData[String(year)]};
  const monthlyData = {...yearlyData[String(month)]};

  let daysCount = 0; // count days in given month
  if (month % 2) {
    daysCount = 31;
  } else if (month === 2) {
    if ((year % 4 === 0 && year % 100 !== 0) || year % 400 === 0) {
      daysCount = 29;
    } else {
      daysCount = 28;
    }
  } else {
    daysCount = 30;
  }

  const modifiedMonthlyData: any = {};
  let day = new Date(`${year}-${month}-${1}`).getDay();
  let weekNo = 0;

  // modify monthly data to contain weekly data
  for (let date = 1; date <= daysCount; date++) {
    if (day === 0 || date === 1) {
      weekNo += 1;
      modifiedMonthlyData[String(weekNo)] = {};
    }
    if (monthlyData[String(date)] !== undefined) {
      modifiedMonthlyData[String(weekNo)][String(date)] =
        monthlyData[String(date)];
    }
    day = (day + 1) % 7;
  }

  for (const week in modifiedMonthlyData) {
    modifiedMonthlyData[week] = getAggregatedData(modifiedMonthlyData[week]);
  }
  console.log(screenTimeData);
  return {
    aggregatedMonthlyData: getAggregatedData(modifiedMonthlyData),
    monthlyData: modifiedMonthlyData,
  };
};

const getYearlyData = (screenTimeData: any, year: number) => {
  const yearlyData = {...screenTimeData[String(year)]};
  for (const month in yearlyData) {
    yearlyData[month] = getAggregatedData(yearlyData[month]);
  }
  console.log(screenTimeData);
  return {
    aggregatedYearlyData: getAggregatedData(yearlyData),
    yearlyData,
  };
};

const getAggregatedData = (data: any) => {
  const aggregatedData: any = {};
  for (const key in data) {
    const value = data[key];
    for (const app in value) {
      if (aggregatedData[app] === undefined) {
        aggregatedData[app] = 0;
      }
      aggregatedData[app] += value[app];
    }
  }
  return aggregatedData;
};

export {
  convertTimeFormat,
  getTotalScreenTime,
  convertToTimeStamp,
  decodeEncodedURI,
  calculateTotalTime,
  getYearlyData,
  getMonthlyData,
  getWeeklyData,
};
